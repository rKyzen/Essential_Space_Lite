package com.essential.spacelite.capture

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Detects the image that was just created by the system screenshot action.
 *
 * Some OEM builds do not expose a stable "Screenshot" relative path or name,
 * so we bias toward "latest recent readable image after trigger" instead of
 * relying on one folder naming convention.
 */
class ScreenshotObserver(
    private val context: Context,
    private val triggerTimeMs: Long,
    private val onDetected: (Uri) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var registered = false
    private var delivered = false

    private val observer = object : ContentObserver(mainHandler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (delivered) return
            if (uri != null) {
                scope.launch { checkUri(uri) }
            } else {
                scope.launch { pollScan() }
            }
        }

        override fun onChange(selfChange: Boolean) {
            if (delivered) return
            scope.launch { pollScan() }
        }
    }

    fun start() {
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        registered = true

        scope.launch {
            var elapsed = 0L
            while (elapsed < 11_500L && !delivered) {
                delay(350)
                elapsed += 350
                pollScan()
            }
            stop()
        }
    }

    fun stop() {
        if (registered) {
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (_: Exception) {
            }
            registered = false
        }
        scope.cancel()
    }

    private suspend fun checkUri(uri: Uri) {
        if (delivered) return
        val info = loadInfo(uri) ?: return
        if (isValidCandidate(info)) {
            deliver(uri)
        }
    }

    private suspend fun pollScan() {
        if (delivered) return

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.MIME_TYPE
        )
        val minSec = ((triggerTimeMs - 3_000L) / 1000L).toString()
        val selection = "${MediaStore.Images.Media.DATE_MODIFIED} >= ?"
        val selectionArgs = arrayOf(minSec)
        val sort = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sort
            )?.use { cursor ->
                while (cursor.moveToNext() && !delivered) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val info = MediaInfo(
                        uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()),
                        dateModifiedSec = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)),
                        size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)),
                        displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)).orEmpty(),
                        relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)).orEmpty(),
                        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)).orEmpty()
                    )
                    if (isValidCandidate(info)) {
                        deliver(info.uri)
                        return
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private suspend fun loadInfo(uri: Uri): MediaInfo? {
        return try {
            val projection = arrayOf(
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.MIME_TYPE
            )
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return null
                MediaInfo(
                    uri = uri,
                    dateModifiedSec = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)),
                    size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)),
                    displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)).orEmpty(),
                    relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)).orEmpty(),
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)).orEmpty()
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun isValidCandidate(info: MediaInfo): Boolean {
        val modMs = info.dateModifiedSec * 1000L
        val withinWindow = modMs >= (triggerTimeMs - 3_000L) && modMs <= (triggerTimeMs + 10_000L)
        val hasContent = info.size > 10_240L
        val isImage = info.mimeType.startsWith("image/")
        val notFromAppScopedMirror = !info.displayName.startsWith("ss_", ignoreCase = true) &&
            !info.displayName.startsWith("thumb_", ignoreCase = true)

        if (!withinWindow || !hasContent || !isImage || !notFromAppScopedMirror) {
            return false
        }

        return canOpen(info.uri)
    }

    private suspend fun canOpen(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(32)
                input.read(buffer) > 0
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun deliver(uri: Uri) {
        if (delivered) return
        delivered = true
        withContext(Dispatchers.Main) {
            onDetected(uri)
        }
        stop()
    }

    private data class MediaInfo(
        val uri: Uri,
        val dateModifiedSec: Long,
        val size: Long,
        val displayName: String,
        val relativePath: String,
        val mimeType: String
    )
}
