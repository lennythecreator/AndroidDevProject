package com.example.gympal

import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.example.gympal.reminders.ReminderScheduler
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class RemindersFragment : Fragment() {

    private lateinit var timePicker: TimePicker
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var reminderStatus: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reminderScheduler = ReminderScheduler(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timePicker = view.findViewById(R.id.reminderTimePicker)
        reminderStatus = view.findViewById(R.id.reminderStatus)
        timePicker.setIs24HourView(false)
        styleTimePicker()

        view.findViewById<MaterialButton>(R.id.scheduleReminderButton).setOnClickListener {
            scheduleReminder()
        }
        view.findViewById<MaterialButton>(R.id.cancelReminderButton).setOnClickListener {
            cancelReminder()
        }
    }

    private fun scheduleReminder() {
        val hour: Int
        val minute: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker.hour
            minute = timePicker.minute
        } else {
            @Suppress("DEPRECATION")
            val legacyHour = timePicker.currentHour
            @Suppress("DEPRECATION")
            val legacyMinute = timePicker.currentMinute
            hour = legacyHour
            minute = legacyMinute
        }

        reminderScheduler.scheduleDailyReminder(hour, minute)
        reminderStatus.text = getString(R.string.reminder_scheduled, hour.formatHour(), minute.formatMinute())
        Toast.makeText(requireContext(), R.string.reminder_scheduled_toast, Toast.LENGTH_SHORT).show()
    }

    private fun cancelReminder() {
        reminderScheduler.cancelReminder()
        reminderStatus.text = getString(R.string.reminder_cancelled_status)
        Toast.makeText(requireContext(), R.string.reminder_cancelled_toast, Toast.LENGTH_SHORT).show()
    }

    private fun styleTimePicker() {
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
        val textSecondary = ContextCompat.getColor(requireContext(), R.color.colorTextSecondary)
        val accent = ContextCompat.getColor(requireContext(), R.color.colorAccent)

        val ids = listOf("hour", "minute", "amPm")
        ids.forEach { name ->
            val viewId = resources.getIdentifier(name, "id", "android")
            val view = timePicker.findViewById<View>(viewId)
            if (view is NumberPicker) {
                tintNumberPicker(view, textPrimary, accent)
            } else if (view is TextView) {
                view.setTextColor(textPrimary)
            }
        }

        val separatorId = resources.getIdentifier("separator", "id", "android")
        val separator = timePicker.findViewById<TextView>(separatorId)
        separator?.setTextColor(textSecondary)
    }

    private fun tintNumberPicker(numberPicker: NumberPicker, textColor: Int, highlightColor: Int) {
        for (i in 0 until numberPicker.childCount) {
            val child = numberPicker.getChildAt(i)
            if (child is EditText) {
                child.setTextColor(textColor)
                child.setHighlightColor(highlightColor)
            }
        }
        try {
            val wheelPaintField = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
            wheelPaintField.isAccessible = true
            val paint = wheelPaintField.get(numberPicker) as Paint
            paint.color = textColor
        } catch (_: Exception) {
        }
        numberPicker.invalidate()
    }

    private fun Int.formatHour(): String = String.format("%02d", this)
    private fun Int.formatMinute(): String = String.format("%02d", this)
}

