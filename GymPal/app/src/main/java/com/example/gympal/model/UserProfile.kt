package com.example.gympal.model

data class UserProfile(
    val name: String,
    val age: Int,
    val weightKg: Float,
    val heightCm: Float,
    val activeness: String,
    val fitnessGoals: String
)