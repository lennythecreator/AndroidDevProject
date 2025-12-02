package com.example.gympal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun WorkoutFormScreen(
    onSave: (WorkoutSession) -> Unit
) {
    var workoutDay by remember { mutableStateOf("") }

    // Start with 1 empty exercise row. This state now holds an immutable List.
    var exercises by remember { mutableStateOf(listOf(Exercise())) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text("Workout Log", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // DAY FIELD
        OutlinedTextField(
            value = workoutDay,
            onValueChange = { workoutDay = it },
            label = { Text("Day of workout (e.g., Monday)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Exercises", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(10.dp))

        // LIST OF EXERCISES
        exercises.forEachIndexed { index, exercise ->
            ExerciseInputCard(
                index = index,
                exercise = exercise,
                onChange = { updatedExercise ->
                    // Create a mutable copy of the current list
                    val updatedList = exercises.toMutableList()
                    // Update the exercise at the specific index
                    updatedList[index] = updatedExercise
                    // Assign the new list back to the state to trigger recomposition
                    exercises = updatedList
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // ADD EXERCISE BUTTON
        Button(
            onClick = {
                // Create a new list by adding a new Exercise to the old list
                exercises = exercises + Exercise()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Another Exercise")
        }

        Spacer(modifier = Modifier.height(25.dp))

        // SAVE BUTTON
        Button(
            onClick = {
                val session = WorkoutSession(
                    day = workoutDay,
                    exercises = exercises
                )
                onSave(session)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Workout")
        }
    }
}

@Composable
fun ExerciseInputCard(
    index: Int,
    exercise: Exercise,
    onChange: (Exercise) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text("Exercise ${index + 1}", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = exercise.name,
                onValueChange = { onChange(exercise.copy(name = it)) },
                label = { Text("Exercise name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                // Reps Field
                OutlinedTextField(
                    value = if (exercise.reps == 0) "" else exercise.reps.toString(),
                    onValueChange = {
                        onChange(exercise.copy(reps = it.toIntOrNull() ?: 0))
                    },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                // Sets Field
                OutlinedTextField(
                    value = if (exercise.sets == 0) "" else exercise.sets.toString(),
                    onValueChange = {
                        onChange(exercise.copy(sets = it.toIntOrNull() ?: 0))
                    },
                    label = { Text("Sets") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration Field
            OutlinedTextField(
                value = if (exercise.duration == 0) "" else exercise.duration.toString(),
                onValueChange = {
                    onChange(exercise.copy(duration = it.toIntOrNull() ?: 0))
                },
                label = { Text("Duration (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
