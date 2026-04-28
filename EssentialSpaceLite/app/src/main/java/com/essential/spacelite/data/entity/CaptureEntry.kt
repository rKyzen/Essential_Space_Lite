package com.essential.spacelite.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capture_entries")
data class CaptureEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "screenshot_path")
    val screenshotPath: String,

    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String,

    @ColumnInfo(name = "text_note")
    val textNote: String? = null,

    @ColumnInfo(name = "voice_note_path")
    val voiceNotePath: String? = null,

    @ColumnInfo(name = "voice_note_duration_ms")
    val voiceNoteDurationMs: Long = 0L,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "app_name")
    val appName: String? = null
)
