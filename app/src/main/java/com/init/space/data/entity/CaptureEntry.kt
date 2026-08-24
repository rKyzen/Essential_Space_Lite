package com.init.space.data.entity

data class CaptureEntry(
    val id: Long = 0,
    val screenshotPath: String,
    val thumbnailPath: String,
    val textNote: String? = null,
    val voiceNotePath: String? = null,
    val voiceNoteDurationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val reminderAt: Long? = null,
    val aiSummary: String? = null,
    val appName: String? = null,
    val isFavorite: Boolean = false
)
