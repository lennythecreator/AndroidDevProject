package com.example.gympal

data class WorkoutSession(
    val day: String = "",
    val exercises: List<Exercise> = emptyList()
)