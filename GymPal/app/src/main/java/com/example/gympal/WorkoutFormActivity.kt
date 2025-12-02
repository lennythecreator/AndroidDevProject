package com.example.gympal

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gympal.model.Exercise
import com.example.gympal.model.WorkoutSession

class WorkoutFormActivity : AppCompatActivity() {

    private lateinit var etWorkoutDay: EditText
    private lateinit var layoutExercisesContainer: LinearLayout
    private lateinit var btnAddExercise: Button
    private lateinit var btnSaveWorkout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_form)

        etWorkoutDay = findViewById(R.id.etWorkoutDay)
        layoutExercisesContainer = findViewById(R.id.layoutExercisesContainer)
        btnAddExercise = findViewById(R.id.btnAddExercise)
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout)

        // Start with one empty exercise, like your Compose version
        addExerciseView()

        btnAddExercise.setOnClickListener {
            addExerciseView()
        }

        btnSaveWorkout.setOnClickListener {
            val session = collectWorkoutSession()

            // For now just show a toast. Later you can send this to backend or back to Dashboard.
            Toast.makeText(
                this,
                "Saved ${session.exercises.size} exercises for ${session.day.ifBlank { "unknown day" }}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addExerciseView() {
        val inflater = LayoutInflater.from(this)
        val exerciseView = inflater.inflate(
            R.layout.item_exercise_input,
            layoutExercisesContainer,
            false
        )

        val titleText = exerciseView.findViewById<TextView>(R.id.tvExerciseTitle)
        val index = layoutExercisesContainer.childCount + 1
        titleText.text = "Exercise $index"

        layoutExercisesContainer.addView(exerciseView)
    }

    private fun collectWorkoutSession(): WorkoutSession {
        val day = etWorkoutDay.text.toString().trim()
        val exercises = mutableListOf<Exercise>()

        for (i in 0 until layoutExercisesContainer.childCount) {
            val exerciseView = layoutExercisesContainer.getChildAt(i)

            val nameEt = exerciseView.findViewById<EditText>(R.id.etExerciseName)
            val repsEt = exerciseView.findViewById<EditText>(R.id.etReps)
            val setsEt = exerciseView.findViewById<EditText>(R.id.etSets)
            val durationEt = exerciseView.findViewById<EditText>(R.id.etDuration)

            val name = nameEt.text.toString().trim()
            val reps = repsEt.text.toString().toIntOrNull() ?: 0
            val sets = setsEt.text.toString().toIntOrNull() ?: 0
            val duration = durationEt.text.toString().toIntOrNull() ?: 0

            // Skip completely empty rows
            if (name.isBlank() && reps == 0 && sets == 0 && duration == 0) continue

            exercises.add(
                Exercise(
                    name = name,
                    reps = reps,
                    sets = sets,
                    duration = duration
                )
            )
        }

        return WorkoutSession(
            day = day,
            exercises = exercises
        )
    }
}