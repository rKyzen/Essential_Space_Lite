package com.init.space

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.init.space.data.AppDatabase

class InitSpaceApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return

        // General channel
        val channel = NotificationChannel(
            CHANNEL_GENERAL,
            "_init_ /space",
            NotificationManager.IMPORTANCE_LOW
        ).also { it.setShowBadge(false) }
        manager.createNotificationChannel(channel)

        // Reminder channel
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            "reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.setShowBadge(true)
            it.enableVibration(true)
            it.vibrationPattern = longArrayOf(0L, 180L, 100L, 180L)
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
        const val CHANNEL_GENERAL = "init_space_general"
        const val CHANNEL_REMINDERS = "init_space_reminders"
    }
}
