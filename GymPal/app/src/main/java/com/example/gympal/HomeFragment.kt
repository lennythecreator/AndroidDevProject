package com.example.gympal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gympal.network.ApiClient
import com.example.gympal.network.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeFragment : Fragment() {

    private lateinit var layoutLoggedOut: View
    private lateinit var layoutLoggedIn: View

    private lateinit var tvGreeting: TextView
    private lateinit var tvAvgWorkouts: TextView
    private lateinit var tvAvgSets: TextView
    private lateinit var tvAvgReps: TextView
    private lateinit var tvAvgDuration: TextView
    private lateinit var tvCurrentGoals: TextView
    private lateinit var btnAiInsights: Button
    private lateinit var btnAddWorkout: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutLoggedOut = view.findViewById(R.id.layoutLoggedOut)
        layoutLoggedIn = view.findViewById(R.id.layoutLoggedIn)

        val btnGoToLogin = view.findViewById<Button>(R.id.btnGoToLogin)
        val btnGoToSignUp = view.findViewById<Button>(R.id.btnGoToSignUp)

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        btnGoToSignUp.setOnClickListener {
            startActivity(Intent(requireContext(), SignUpActivity::class.java))
        }

        val authPrefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val loggedIn = authPrefs.getBoolean("logged_in", false)

        if (loggedIn) {
            showDashboard(view)
        } else {
            showLoggedOut()
        }
    }

    private fun showLoggedOut() {
        layoutLoggedOut.visibility = View.VISIBLE
        layoutLoggedIn.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun showDashboard(root: View) {
        layoutLoggedOut.visibility = View.GONE
        layoutLoggedIn.visibility = View.VISIBLE

        // Dashboard views are inside the included layout
        tvGreeting = root.findViewById(R.id.tvGreeting)
        tvAvgWorkouts = root.findViewById(R.id.tvAvgWorkouts)
        tvAvgSets = root.findViewById(R.id.tvAvgSets)
        tvAvgReps = root.findViewById(R.id.tvAvgReps)
        tvAvgDuration = root.findViewById(R.id.tvAvgDuration)
        tvCurrentGoals = root.findViewById(R.id.tvCurrentGoals)
        btnAiInsights = root.findViewById(R.id.btnAiInsights)
        btnAddWorkout = root.findViewById(R.id.btnAddWorkout)
        btnLogout = root.findViewById(R.id.btnLogout)

        btnAiInsights.setOnClickListener {
            startActivity(Intent(requireContext(), AIInsightsActivity::class.java))
        }

        btnAddWorkout.setOnClickListener {
            startActivity(Intent(requireContext(), WorkoutFormActivity::class.java))
        }

        btnLogout.setOnClickListener {
            SessionManager.clear(requireContext())
            showLoggedOut()
        }

        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        val userId = SessionManager.userId(requireContext())
        if (userId <= 0) {
            showLoggedOut()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profile = ApiClient.get("/users/$userId/profile")
                val averages = ApiClient.get("/users/$userId/averages")
                val goals = ApiClient.get("/users/$userId/goals/current")
                bindDashboard(profile, averages, goals)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load dashboard", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindDashboard(profile: JSONObject, averages: JSONObject, goals: JSONObject) {
        val name = profile.optString("name", "user")
        tvGreeting.text = "Hi, $name"
        tvAvgWorkouts.text = averages.optDouble("workouts_per_week", 0.0).toString()
        tvAvgSets.text = averages.optDouble("avg_sets", 0.0).toString()
        tvAvgReps.text = averages.optDouble("avg_reps", 0.0).toString()
        val duration = averages.optDouble("avg_duration_minutes", 0.0)
        tvAvgDuration.text = "${duration} min"

        val goalsList = goals.optJSONArray("goals")
        if (goalsList != null && goalsList.length() > 0) {
            val builder = StringBuilder()
            for (i in 0 until goalsList.length()) {
                builder.append("\u2022 ").append(goalsList.optString(i)).append("\n\n")
            }
            tvCurrentGoals.text = builder.toString().trim()
        } else {
            tvCurrentGoals.text = getString(R.string.no_goals_yet)
        }
    }
}
