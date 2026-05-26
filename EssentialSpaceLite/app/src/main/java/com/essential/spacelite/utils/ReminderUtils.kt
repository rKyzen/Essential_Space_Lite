package com.essential.spacelite.utils

import android.content.Context
import com.essential.spacelite.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReminderUtils {
    private val overviewFormat = SimpleDateFormat("MMM d, h:mm a", Locale.US)
    private val detailFormat = SimpleDateFormat("EEEE, MMM d - h:mm a", Locale.US)

    fun formatOverviewReminder(context: Context, reminderAt: Long?): String {
        return reminderAt?.let { "Remind ${overviewFormat.format(Date(it))}" }
            ?: context.getString(R.string.reminder_never)
    }

    fun formatDetailReminder(context: Context, reminderAt: Long?): String {
        return reminderAt?.let { detailFormat.format(Date(it)) }
            ?: context.getString(R.string.reminder_never)
    }
}
