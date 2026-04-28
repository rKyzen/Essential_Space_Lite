package com.essential.spacelite.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    private const val SCREENSHOT_DIR = "screenshots"
    private const val THUMBNAIL_DIR = "thumbnails"
    private const val VOICE_DIR = "voice_notes"
    private const val THUMBNAIL_MAX_SIZE = 300

    fun getScreenshotDir(context: Context): File {
        return File(context.filesDir, SCREENSHOT_DIR).also { it.mkdirs() }
    }

    fun getThumbnailDir(context: Context): File {
        return File(context.filesDir, THUMBNAIL_DIR).also { it.mkdirs() }
    }

    fun getVoiceDir(context: Context): File {
        return File(context.filesDir, VOICE_DIR).also { it.mkdirs() }
    }

    fun generateFileName(prefix: String, ext: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return "${prefix}_${timestamp}.${ext}"
    }

    /**
     * Saves full screenshot and compressed thumbnail.
     * Returns pair of (screenshotPath, thumbnailPath)
     */
    fun saveCaptureFiles(context: Context, bitmap: Bitmap): Pair<String, String> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())

        // Save full screenshot (compressed JPEG, quality 85)
        val screenshotFile = File(getScreenshotDir(context), "ss_${timestamp}.jpg")
        FileOutputStream(screenshotFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        // Generate and save thumbnail
        val thumbnail = createThumbnail(bitmap)
        val thumbnailFile = File(getThumbnailDir(context), "thumb_${timestamp}.jpg")
        FileOutputStream(thumbnailFile).use { out ->
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }
        thumbnail.recycle()

        return Pair(screenshotFile.absolutePath, thumbnailFile.absolutePath)
    }

    private fun createThumbnail(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val scale = THUMBNAIL_MAX_SIZE.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }

    fun newVoiceNotePath(context: Context): String {
        val file = File(getVoiceDir(context), generateFileName("voice", "m4a"))
        return file.absolutePath
    }

    fun deleteFile(path: String?) {
        path?.let { File(it).delete() }
    }

    fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return if (min > 0) "${min}m ${sec}s" else "${sec}s"
    }
}
