package com.example.gympal

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToSignUp: TextView
    private var isLoginInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp)

        btnLogin.setOnClickListener {
            handleLogin()
        }

        tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun handleLogin() {
        // Prevent multiple simultaneous login attempts
        if (isLoginInProgress) {
            return
        }

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Check network connectivity before attempting login
        if (!isNetworkAvailable()) {
            Toast.makeText(
                this,
                "No internet connection. Please check your network settings.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        isLoginInProgress = true
        btnLogin.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val payload = JSONObject()
                    .put("email", email)
                    .put("password", password)
                val response = ApiClient.post("/login", payload)
                val userId = response.optInt("user_id", -1)
                if (userId <= 0) {
                    throw IllegalStateException("Invalid response: No user id received")
                }

                SessionManager.saveAuth(this@LoginActivity, userId, email)

                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } catch (e: ApiException) {
                // Handle API errors (401, 404, etc.) with specific messages
                val errorMessage = when (e.statusCode) {
                    401 -> "Invalid email or password"
                    404 -> "User not found"
                    400 -> e.message ?: "Invalid request"
                    else -> "Login failed: ${e.message}"
                }
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
            } catch (e: NetworkException) {
                // Handle network errors with user-friendly messages
                Toast.makeText(this@LoginActivity, e.message ?: "Network error occurred", Toast.LENGTH_LONG).show()
            } catch (e: IllegalStateException) {
                Toast.makeText(this@LoginActivity, e.message ?: "Login failed", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${e.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isLoginInProgress = false
                btnLogin.isEnabled = true
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
