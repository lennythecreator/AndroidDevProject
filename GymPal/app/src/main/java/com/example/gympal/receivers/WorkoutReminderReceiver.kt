package com.example.gympal.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.gympal.R

class WorkoutReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val message =
            intent?.getStringExtra(EXTRA_MESSAGE)
                ?: context.getString(R.string.default_reminder_message)

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_MESSAGE = "extra_message"
    }
}

