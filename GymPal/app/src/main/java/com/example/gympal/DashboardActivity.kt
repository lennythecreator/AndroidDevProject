package com.example.gympal

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvAvgWorkouts: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvAvgSets: TextView
    private lateinit var tvAvgReps: TextView
    private lateinit var tvAvgDuration: TextView
    private lateinit var tvCurrentGoals: TextView
    private lateinit var btnAiInsights: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvGreeting = findViewById(R.id.tvGreeting)
        tvAvgWorkouts = findViewById(R.id.tvAvgWorkouts)
        tvAvgSets = findViewById(R.id.tvAvgSets)
        tvAvgReps = findViewById(R.id.tvAvgReps)
        tvAvgDuration = findViewById(R.id.tvAvgDuration)
        tvCurrentGoals = findViewById(R.id.tvCurrentGoals)
        btnAiInsights = findViewById(R.id.btnAiInsights)

        // Mock data for now
        val avgWorkouts = 4.2
        val avgSets = 12.5
        val avgReps = 96.0
        val avgDuration = 65
        val currentGoals = listOf(
            "Workout 4 times a week",
            "Average 10 sets per workout",
            "Hit at least 80 reps per session",
            "Train for 60 minutes each workout"
        )

        tvGreeting.text = "Hi, Tobi"
        tvAvgWorkouts.text = "$avgWorkouts"
        tvAvgSets.text = "$avgSets"
        tvAvgReps.text = "$avgReps"
        tvAvgDuration.text = "$avgDuration min"

        val goalsText = currentGoals.joinToString(separator = "\n\n") { goal ->
            "• $goal"
        }
        tvCurrentGoals.text = goalsText

        btnAiInsights.setOnClickListener {
            // Later: navigate to AIInsightsActivity
        }
    }
}