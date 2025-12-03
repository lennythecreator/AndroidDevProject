package com.example.gympal.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object ApiClient {
    // Point to the exposed backend tunnel; update if the tunnel URL changes.
    private const val BASE_URL = "https://kd35z1pp-8000.usw3.devtunnels.ms"
    private val client = OkHttpClient()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun post(path: String, payload: JSONObject): JSONObject {
        return withContext(Dispatchers.IO) {
            val body = payload.toString().toRequestBody(jsonMedia)
            val request = Request.Builder()
                .url(BASE_URL + path)
                .post(body)
                .build()
            execute(request)
        }
    }

    suspend fun get(path: String): JSONObject {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(BASE_URL + path)
                .get()
                .build()
            execute(request)
        }
    }

    private fun execute(request: Request): JSONObject {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: "{}"
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: $body")
            }
            return try {
                JSONObject(body)
            } catch (_: Exception) {
                JSONObject()
            }
        }
    }
}
