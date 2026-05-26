package com.essential.spacelite.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.essential.spacelite.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object GeminiSummaryClient {

    private const val API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/"
    private val fallbackModels = listOf(
        "gemini-flash-latest",
        "gemini-2.0-flash",
        "gemini-1.5-flash-latest"
    )

    fun isConfigured(): Boolean = BuildConfig.GEMINI_API_KEY.isNotBlank()

    fun generateSummary(
        screenshotPath: String,
        note: String?,
        reminderAt: Long?
    ): Result<String> {
        if (!isConfigured()) {
            return Result.failure(IllegalStateException("Gemini API key is missing"))
        }

        val imageData = prepareImageData(File(screenshotPath))
            ?: return Result.failure(IllegalStateException("Could not read screenshot for summary"))

        val prompt = buildPrompt(note, reminderAt)
        val payload = buildPayload(prompt, imageData)
        val modelsToTry = buildList {
            add(BuildConfig.GEMINI_MODEL)
            addAll(fallbackModels)
        }.distinct()

        var lastError: Throwable? = null
        for (model in modelsToTry) {
            val result = requestSummary(model, payload)
            if (result.isSuccess) {
                return result
            }

            val error = result.exceptionOrNull()
            if (error !is GeminiRequestException || error.statusCode != 404) {
                return result
            }
            lastError = error
        }

        return Result.failure(lastError ?: IllegalStateException("Gemini request failed"))
    }

    private fun buildPayload(prompt: String, imageData: String): JSONObject {
        return JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().apply {
                        put(
                            "parts",
                            JSONArray()
                                .put(JSONObject().put("text", prompt))
                                .put(
                                    JSONObject().put(
                                        "inline_data",
                                        JSONObject()
                                            .put("mime_type", "image/jpeg")
                                            .put("data", imageData)
                                    )
                                )
                        )
                    }
                )
            )
        }
    }

    private fun requestSummary(model: String, payload: JSONObject): Result<String> {
        val endpoint = "${API_BASE}${model}:generateContent"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 30_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("X-goog-api-key", BuildConfig.GEMINI_API_KEY)
        }

        return runCatching {
            connection.outputStream.use { out ->
                out.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: throw GeminiRequestException(
                    statusCode = responseCode,
                    uiMessage = "Gemini request failed"
                )
            }

            val response = stream.bufferedReader().use { it.readText() }
            if (responseCode !in 200..299) {
                throw parseApiError(responseCode, response)
            }

            parseSummary(response)
        }.also {
            connection.disconnect()
        }
    }

    private fun buildPrompt(note: String?, reminderAt: Long?): String {
        val noteText = note?.takeIf { it.isNotBlank() } ?: "No extra note was added."
        val reminderText = reminderAt?.let { "A reminder is set for timestamp $it." } ?: "No reminder is set."
        return """
            You are summarizing a saved screenshot for a personal capture app.
            Combine the screenshot content with the user's note.
            Keep it concise, clear, and helpful.
            Mention the likely context, the most important visual elements, and the user's intent.
            Avoid bullet points. Write 2 to 4 short sentences.

            User note: $noteText
            Reminder: $reminderText
        """.trimIndent()
    }

    private fun prepareImageData(file: File): String? {
        if (!file.exists()) return null
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        val scaled = scaleBitmap(bitmap, 1280)
        if (scaled !== bitmap) {
            bitmap.recycle()
        }
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, output)
        scaled.recycle()
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    private fun scaleBitmap(source: Bitmap, maxSide: Int): Bitmap {
        val width = source.width
        val height = source.height
        val largest = maxOf(width, height)
        if (largest <= maxSide) return source
        val scale = maxSide.toFloat() / largest.toFloat()
        return Bitmap.createScaledBitmap(
            source,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun parseSummary(response: String): String {
        val root = JSONObject(response)
        val candidates = root.optJSONArray("candidates")
            ?: throw IllegalStateException("Gemini returned no candidates")
        val firstCandidate = candidates.optJSONObject(0)
            ?: throw IllegalStateException("Gemini returned an empty candidate")
        val content = firstCandidate.optJSONObject("content")
            ?: throw IllegalStateException("Gemini returned no content")
        val parts = content.optJSONArray("parts")
            ?: throw IllegalStateException("Gemini returned no text parts")
        val text = buildString {
            for (index in 0 until parts.length()) {
                append(parts.optJSONObject(index)?.optString("text").orEmpty())
            }
        }.trim()
        if (text.isBlank()) {
            throw IllegalStateException("Gemini returned an empty summary")
        }
        return text
    }

    private fun parseApiError(statusCode: Int, response: String): GeminiRequestException {
        return try {
            val error = JSONObject(response).optJSONObject("error")
            val message = error?.optString("message").orEmpty().ifBlank {
                "Gemini request failed"
            }
            GeminiRequestException(
                statusCode = statusCode,
                uiMessage = humanizeApiMessage(statusCode, message)
            )
        } catch (_: Exception) {
            GeminiRequestException(
                statusCode = statusCode,
                uiMessage = "Gemini request failed"
            )
        }
    }

    private fun humanizeApiMessage(statusCode: Int, raw: String): String {
        return when {
            statusCode == 404 -> "This Gemini model is unavailable. Trying another supported model."
            statusCode == 401 || statusCode == 403 -> "The Gemini API key was rejected. Check the key and API access."
            raw.contains("quota", ignoreCase = true) -> "Gemini quota was reached. Try again later."
            else -> raw
        }
    }

    private class GeminiRequestException(
        val statusCode: Int,
        val uiMessage: String
    ) : IllegalStateException(uiMessage)
}
