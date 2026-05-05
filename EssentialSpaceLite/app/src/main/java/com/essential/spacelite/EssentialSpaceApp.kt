package com.essential.spacelite

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
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
        manager.deleteNotificationChannel("origin_reminders")
        // General channel for future notifications
        val channel = NotificationChannel(
            CHANNEL_GENERAL,
            "Essential Space",
            NotificationManager.IMPORTANCE_LOW
        ).also { it.setShowBadge(false) }
        manager.createNotificationChannel(channel)

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Origin reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.setShowBadge(true)
            it.enableVibration(true)
            it.vibrationPattern = longArrayOf(0L, 220L, 140L, 220L)
            it.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        manager.createNotificationChannel(reminderChannel)
    }

    companion object {
        const val CHANNEL_GENERAL = "essential_general"
        const val CHANNEL_REMINDERS = "origin_reminders_v2"
    }
}
