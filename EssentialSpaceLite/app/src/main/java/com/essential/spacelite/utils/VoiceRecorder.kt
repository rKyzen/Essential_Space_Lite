package com.essential.spacelite.utils

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputPath: String? = null
    private var startTime = 0L

    var isRecording = false
        private set

    val durationMs: Long
        get() = if (isRecording) System.currentTimeMillis() - startTime else 0L

    fun start(path: String): Boolean {
        return try {
            outputPath = path
            recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION") MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(path)
                setMaxDuration(60_000)
                prepare()
                start()
            }
            startTime = System.currentTimeMillis()
            isRecording = true
            true
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            false
        }
    }

    /** Stop and return duration ms, or null on failure. */
    fun stop(): Long? {
        if (!isRecording) return null
        return try {
            val duration = System.currentTimeMillis() - startTime
            recorder?.stop()
            recorder?.release()
            recorder = null
            isRecording = false
            duration
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            isRecording = false
            outputPath?.let { File(it).delete() }
            outputPath = null
            null
        }
    }

    fun cancel() {
        try { recorder?.stop() } catch (_: Exception) {}
        recorder?.release()
        recorder = null
        isRecording = false
        outputPath?.let { File(it).delete() }
        outputPath = null
    }

    fun release() {
        recorder?.release()
        recorder = null
        isRecording = false
    }
}
