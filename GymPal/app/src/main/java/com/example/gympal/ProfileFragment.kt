package com.example.gympal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gympal.network.ApiClient
import com.example.gympal.network.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvActiveness: TextView
    private lateinit var tvGoals: TextView
    private lateinit var tvConsistency: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvName = view.findViewById(R.id.profileNameValue)
        tvAge = view.findViewById(R.id.profileAgeValue)
        tvWeight = view.findViewById(R.id.profileWeightValue)
        tvHeight = view.findViewById(R.id.profileHeightValue)
        tvActiveness = view.findViewById(R.id.profileActivenessValue)
        tvGoals = view.findViewById(R.id.profileGoalsValue)
        tvConsistency = view.findViewById(R.id.profileConsistencyValue)

        bindLocalProfile()
        fetchProfile()
    }

    private fun bindLocalProfile() {
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        tvName.text = prefs.getString("name", "—")
        tvAge.text = prefs.getInt("age", 0).takeIf { it > 0 }?.toString() ?: "—"
        val weight = prefs.getFloat("weightKg", 0f)
        tvWeight.text = if (weight > 0f) "${weight} kg" else "—"
        val height = prefs.getFloat("heightCm", 0f)
        tvHeight.text = if (height > 0f) "${height} cm" else "—"
        val activeness = prefs.getString("activeness", "") ?: ""
        tvActiveness.text = if (activeness.isNotBlank()) activeness else "—"
        val goals = prefs.getString("fitnessGoals", "") ?: ""
        tvGoals.text = if (goals.isNotBlank()) goals else "—"
        tvConsistency.text = "—"
    }

    private fun fetchProfile() {
        val userId = SessionManager.userId(requireContext())
        if (userId <= 0) {
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profile = ApiClient.get("/users/$userId/profile")
                val averages = ApiClient.get("/users/$userId/averages")
                bindProfile(profile, averages)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindProfile(profile: JSONObject, averages: JSONObject) {
        tvName.text = profile.optString("name", "User")
        tvAge.text = profile.optInt("age", 0).toString()
        val weight = profile.optDouble("weight_kg", 0.0)
        tvWeight.text = if (weight > 0) "$weight kg" else "—"
        val height = profile.optDouble("height_cm", 0.0)
        tvHeight.text = if (height > 0) "$height cm" else "—"
        val activeness = profile.optString("activeness", "")
        tvActiveness.text = if (activeness.isNotBlank()) activeness else "—"
        val goals = profile.optString("fitness_goals", "")
        tvGoals.text = if (goals.isNotBlank()) goals else "No goals yet"

        val workoutsPerWeek = averages.optDouble("workouts_per_week", 0.0)
        val consistency = kotlin.math.min(100.0, (workoutsPerWeek / 5.0) * 100)
        tvConsistency.text = "${"%.0f".format(consistency)}%"
    }
}
