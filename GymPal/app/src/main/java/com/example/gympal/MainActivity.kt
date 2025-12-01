package com.example.gympal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WorkoutFormScreen(
                onSave = { session ->
                    // TODO: send data to backend here
                    println(session)
                }
            )
        }
    }
}
