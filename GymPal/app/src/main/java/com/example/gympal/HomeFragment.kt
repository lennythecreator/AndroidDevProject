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
import androidx.fragment.app.Fragment

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
        btnLogout = root.findViewById(R.id.btnLogout)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val name = prefs.getString("name", "user") ?: "user"
        tvGreeting.text = "Hi, $name"

        // You can plug your real data here later
        tvAvgWorkouts.text = "0"
        tvAvgSets.text = "0"
        tvAvgReps.text = "0"
        tvAvgDuration.text = "0 min"
        tvCurrentGoals.text = getString(R.string.no_goals_yet)

        btnAiInsights.setOnClickListener {
            startActivity(Intent(requireContext(), AIInsightsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val authPrefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val userPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            authPrefs.edit().clear().apply()
            userPrefs.edit().clear().apply()
            showLoggedOut()
        }
    }
}
