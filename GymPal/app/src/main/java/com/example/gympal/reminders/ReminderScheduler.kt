package com.example.gympal.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.gympal.R
import com.example.gympal.receivers.WorkoutReminderReceiver
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val triggerTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_DAY,
            reminderPendingIntent()
        )
    }

    fun cancelReminder() {
        alarmManager.cancel(reminderPendingIntent())
    }

    private fun reminderPendingIntent(): PendingIntent {
        val intent = Intent(context, WorkoutReminderReceiver::class.java).apply {
            putExtra(
                WorkoutReminderReceiver.EXTRA_MESSAGE,
                context.getString(R.string.default_reminder_message)
            )
        }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val REQUEST_CODE = 4123
    }
}

