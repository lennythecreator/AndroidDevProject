package com.example.gympal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gympal.model.UserProfile
import androidx.core.content.edit

class OnboardingActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var etActiveness: EditText
    private lateinit var etFitnessGoals: EditText
    private lateinit var btnContinue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        etName = findViewById(R.id.etName)
        etAge = findViewById(R.id.etAge)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        etActiveness = findViewById(R.id.etActiveness)
        etFitnessGoals = findViewById(R.id.etFitnessGoals)
        btnContinue = findViewById(R.id.btnContinue)

        btnContinue.setOnClickListener {
            saveProfileAndContinue()
        }
    }

    private fun saveProfileAndContinue() {
        val name = etName.text.toString().trim()
        val age = etAge.text.toString().trim().toIntOrNull() ?: 0
        val weight = etWeight.text.toString().trim().toFloatOrNull() ?: 0f
        val height = etHeight.text.toString().trim().toFloatOrNull() ?: 0f
        val activeness = etActiveness.text.toString().trim()
        val fitnessGoals = etFitnessGoals.text.toString().trim()

        if (name.isBlank()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = UserProfile(
            name = name,
            age = age,
            weightKg = weight,
            heightCm = height,
            activeness = activeness,
            fitnessGoals = fitnessGoals
        )

        saveUserProfile(profile)

        // After onboarding, go to the main nav-hosted experience (keeps bottom nav visible)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveUserProfile(profile: UserProfile) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit {
            putString("name", profile.name)
                .putInt("age", profile.age)
                .putFloat("weightKg", profile.weightKg)
                .putFloat("heightCm", profile.heightCm)
                .putString("activeness", profile.activeness)
                .putString("fitnessGoals", profile.fitnessGoals)
                .putBoolean("onboarding_complete", true)
        }
    }
}
