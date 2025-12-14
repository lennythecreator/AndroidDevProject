package com.example.gympal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.gympal.model.UserProfile
import com.example.gympal.network.ApiClient
import com.example.gympal.network.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

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

        val userId = SessionManager.userId(this)
        if (userId <= 0) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            return
        }

        btnContinue.isEnabled = false
        lifecycleScope.launch {
            try {
                val goalsArray = JSONArray()
                if (fitnessGoals.isNotBlank()) {
                    fitnessGoals.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        .forEach { goalsArray.put(it) }
                }

                val payload = JSONObject()
                    .put("user_id", userId)
                    .put("name", name)
                    .put("age", age)
                    .put("weight_kg", weight)
                    .put("height_cm", height)
                    .put("activeness", activeness)
                    .put("fitness_goals", fitnessGoals)
                    .put("goals", goalsArray)

                ApiClient.post("/onboarding", payload)
                saveUserProfile(profile)

                val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@OnboardingActivity, "Failed to save onboarding", Toast.LENGTH_SHORT).show()
            } finally {
                btnContinue.isEnabled = true
            }
        }
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
