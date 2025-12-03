package com.example.gympal.network

import android.content.Context

object SessionManager {
    private const val AUTH_PREFS = "auth_prefs"
    private const val USER_PREFS = "user_prefs"

    fun saveAuth(context: Context, userId: Int, email: String) {
        val prefs = context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("logged_in", true)
            .putInt("user_id", userId)
            .putString("email", email)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            .getBoolean("logged_in", false)

    fun userId(context: Context): Int =
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            .getInt("user_id", -1)

    fun clear(context: Context) {
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
