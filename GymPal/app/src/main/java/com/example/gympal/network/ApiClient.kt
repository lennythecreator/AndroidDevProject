package com.example.gympal.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object ApiClient {
    // Point to the exposed backend tunnel; update if the tunnel URL changes.
    private const val BASE_URL = "https://kd35z1pp-8000.usw3.devtunnels.ms"
    
    // Configure OkHttpClient with proper timeouts and retry logic
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS) // Time to establish connection
        .readTimeout(30, TimeUnit.SECONDS)    // Time to read response
        .writeTimeout(30, TimeUnit.SECONDS)   // Time to write request
        .retryOnConnectionFailure(true)       // Automatically retry on connection failures
        .build()
    
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    
    // Maximum number of retry attempts for transient failures
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 1000L

    suspend fun post(path: String, payload: JSONObject): JSONObject {
        return withContext(Dispatchers.IO) {
            val body = payload.toString().toRequestBody(jsonMedia)
            val request = Request.Builder()
                .url(BASE_URL + path)
                .post(body)
                .build()
            executeWithRetry(request)
        }
    }

    suspend fun get(path: String): JSONObject {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(BASE_URL + path)
                .get()
                .build()
            executeWithRetry(request)
        }
    }

    private suspend fun executeWithRetry(request: Request): JSONObject {
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                return execute(request)
            } catch (e: SocketTimeoutException) {
                lastException = e
                if (attempt < MAX_RETRIES - 1) {
                    kotlinx.coroutines.delay(RETRY_DELAY_MS * (attempt + 1)) // Exponential backoff
                }
            } catch (e: UnknownHostException) {
                // No point retrying if host is unknown
                throw NetworkException("Unable to reach server. Please check your internet connection.", e)
            } catch (e: IOException) {
                lastException = e
                // Only retry on connection-related IOExceptions
                if (attempt < MAX_RETRIES - 1 && isRetryableException(e)) {
                    kotlinx.coroutines.delay(RETRY_DELAY_MS * (attempt + 1))
                } else {
                    throw NetworkException("Network error: ${e.message}", e)
                }
            } catch (e: Exception) {
                // Don't retry on non-network exceptions
                throw e
            }
        }
        
        throw NetworkException("Request failed after $MAX_RETRIES attempts: ${lastException?.message}", lastException)
    }

    private fun execute(request: Request): JSONObject {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: "{}"
            if (!response.isSuccessful) {
                // Try to extract error message from response body
                val errorMessage = try {
                    val errorJson = JSONObject(body)
                    errorJson.optString("detail", "Unknown error")
                } catch (_: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                throw ApiException(response.code, errorMessage)
            }
            return try {
                JSONObject(body)
            } catch (e: Exception) {
                throw NetworkException("Invalid response format: ${e.message}", e)
            }
        }
    }

    private fun isRetryableException(e: IOException): Boolean {
        return e.message?.contains("timeout", ignoreCase = true) == true ||
               e.message?.contains("connection", ignoreCase = true) == true ||
               e.message?.contains("network", ignoreCase = true) == true
    }
}

// Custom exception classes for better error handling
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)
class ApiException(val statusCode: Int, message: String) : Exception(message)
