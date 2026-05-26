package com.essential.spacelite.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.essential.spacelite.data.entity.CaptureEntry
import com.essential.spacelite.reminder.ReminderReceiver

object ReminderScheduler {

    fun schedule(context: Context, entry: CaptureEntry) {
        val reminderAt = entry.reminderAt ?: return
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = buildPendingIntent(context, entry.id)
        alarmManager.cancel(pendingIntent)
        val intentTime = if (reminderAt > System.currentTimeMillis()) reminderAt else System.currentTimeMillis() + 5_000L
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, intentTime, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, intentTime, pendingIntent)
        }
    }

    fun cancel(context: Context, entryId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = buildPendingIntent(context, entryId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun buildPendingIntent(context: Context, entryId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ENTRY_ID, entryId)
        }
        return PendingIntent.getBroadcast(
            context,
            entryId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
