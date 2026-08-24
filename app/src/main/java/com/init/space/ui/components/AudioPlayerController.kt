package com.init.space.ui.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

class AudioPlayerController(private val context: Context) {

    var currentPlayingPath by mutableStateOf<String?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    private var mediaPlayer: MediaPlayer? = null

    fun togglePlay(path: String) {
        if (currentPlayingPath == path && isPlaying) {
            pause()
            return
        }

        if (currentPlayingPath == path && mediaPlayer != null) {
            mediaPlayer?.start()
            isPlaying = true
            return
        }

        play(path)
    }

    fun play(path: String) {
        stop()
        if (!File(path).exists()) return

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    this@AudioPlayerController.isPlaying = false
                    this@AudioPlayerController.currentPlayingPath = null
                }
                start()
            }
            currentPlayingPath = path
            isPlaying = true
        } catch (_: Exception) {
            stop()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            isPlaying = false
        } catch (_: Exception) {
            stop()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
            currentPlayingPath = null
            isPlaying = false
        }
    }

    fun release() {
        stop()
    }
}
