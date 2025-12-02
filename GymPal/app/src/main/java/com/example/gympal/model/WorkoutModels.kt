package com.example.gympal.model

data class Exercise(
    val name: String = "",
    val reps: Int = 0,
    val sets: Int = 0,
    val duration: Int = 0
)

data class WorkoutSession(
    val day: String = "",
    val exercises: List<Exercise> = emptyList()
)