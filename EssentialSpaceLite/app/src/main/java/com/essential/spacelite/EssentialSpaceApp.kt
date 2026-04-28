package com.essential.spacelite

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.essential.spacelite.data.AppDatabase
import com.essential.spacelite.utils.ThemeHelper

class EssentialSpaceApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        ThemeHelper.applySavedNightMode(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        // General channel for future notifications
        val channel = NotificationChannel(
            CHANNEL_GENERAL,
            "Essential Space",
            NotificationManager.IMPORTANCE_LOW
        ).also { it.setShowBadge(false) }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_GENERAL = "essential_general"
    }
}
