package com.init.space.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.init.space.InitSpaceApp
import com.init.space.R
import com.init.space.data.AppDatabase
import com.init.space.ui.MainActivity
import com.init.space.utils.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val entryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)
        if (entryId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entry = AppDatabase.getDatabase(context).getEntryById(entryId) ?: return@launch
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_OPEN_ENTRY_ID, entryId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val contentIntent = PendingIntent.getActivity(
                    context,
                    entryId.toInt(),
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val text = entry.textNote?.takeIf { it.isNotBlank() } ?: context.getString(R.string.reminder_notification_text)
                val notification = NotificationCompat.Builder(context, InitSpaceApp.CHANNEL_REMINDERS)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(context.getString(R.string.reminder_notification_title))
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build()

                NotificationManagerCompat.from(context).notify(entryId.toInt(), notification)
                AppDatabase.getDatabase(context)
                    .update(entry.copy(reminderAt = null))
                ReminderScheduler.cancel(context, entry.id)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }
}
