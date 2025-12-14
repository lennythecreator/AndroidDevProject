package com.example.gympal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gympal.network.ApiClient
import com.example.gympal.network.ApiException
import com.example.gympal.network.NetworkException
import com.example.gympal.network.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etEmail = findViewById(R.id.etSignUpEmail)
        etPassword = findViewById(R.id.etSignUpPassword)
        etConfirmPassword = findViewById(R.id.etSignUpConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)

        btnSignUp.setOnClickListener {
            handleSignUp()
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun handleSignUp() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        btnSignUp.isEnabled = false
        lifecycleScope.launch {
            try {
                val payload = JSONObject()
                    .put("email", email)
                    .put("password", password)
                val response = ApiClient.post("/signup", payload)
                val userId = response.optInt("user_id", -1)
                if (userId <= 0) throw IllegalStateException("No user id")

                SessionManager.saveAuth(this@SignUpActivity, userId, email)

                // Go to onboarding after sign up
                startActivity(Intent(this@SignUpActivity, OnboardingActivity::class.java))
                finish()
            } catch (e: ApiException) {
                // Handle API errors (4xx, 5xx responses)
                val errorMessage = when {
                    e.statusCode == 400 && e.message?.contains("already registered", ignoreCase = true) == true -> {
                        "This email is already registered. Please use a different email or try logging in."
                    }
                    e.statusCode == 400 -> {
                        "Invalid request: ${e.message ?: "Bad request"}"
                    }
                    e.statusCode == 422 -> {
                        "Invalid email or password format. Please check your input."
                    }
                    e.statusCode in 500..599 -> {
                        "Server error. Please try again later."
                    }
                    else -> {
                        "Sign up failed: ${e.message ?: "Unknown error"}"
                    }
                }
                Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_LONG).show()
            } catch (e: NetworkException) {
                // Handle network errors
                val errorMessage = when {
                    e.message?.contains("Unable to reach server", ignoreCase = true) == true -> {
                        "Unable to connect to server. Please check your internet connection."
                    }
                    e.message?.contains("timeout", ignoreCase = true) == true -> {
                        "Request timed out. Please check your connection and try again."
                    }
                    else -> {
                        "Network error: ${e.message ?: "Unable to connect"}"
                    }
                }
                Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_LONG).show()
            } catch (e: IllegalStateException) {
                // Handle unexpected response format
                Toast.makeText(
                    this@SignUpActivity,
                    "Unexpected error occurred. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                // Catch-all for any other exceptions
                Toast.makeText(
                    this@SignUpActivity,
                    "An error occurred: ${e.message ?: "Please try again"}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnSignUp.isEnabled = true
            }
        }
    }
}
