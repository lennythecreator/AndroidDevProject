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
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import org.json.JSONObject

class InsightsFragment : Fragment() {

    private lateinit var tvIntensity: TextView
    private lateinit var tvCommitment: TextView
    private lateinit var progressIntensity: LinearProgressIndicator
    private lateinit var progressCommitment: LinearProgressIndicator
    private lateinit var progressStrength: CircularProgressIndicator
    private lateinit var progressWeight: CircularProgressIndicator
    private lateinit var tvStrengthPct: TextView
    private lateinit var tvWeightPct: TextView
    private lateinit var tvSuggestion: TextView
    private lateinit var tvCalories: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ai_insights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvIntensity = view.findViewById(R.id.tvIntensityScore)
        tvCommitment = view.findViewById(R.id.tvCommitmentScore)
        progressIntensity = view.findViewById(R.id.progressIntensity)
        progressCommitment = view.findViewById(R.id.progressCommitment)
        progressStrength = view.findViewById(R.id.progressStrength)
        progressWeight = view.findViewById(R.id.progressWeight)
        tvStrengthPct = view.findViewById(R.id.tvStrengthPct)
        tvWeightPct = view.findViewById(R.id.tvWeightPct)
        tvSuggestion = view.findViewById(R.id.tvSuggestion)
        tvCalories = view.findViewById(R.id.tvSuggestedCalories)

        fetchInsights()
    }

    private fun fetchInsights() {
        val userId = SessionManager.userId(requireContext())
        if (userId <= 0) {
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = ApiClient.get("/users/$userId/insights")
                bindInsights(resp)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load insights", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindInsights(insights: JSONObject) {
        val intensity = insights.optDouble("intensity_score", 0.0)
        val commitment = insights.optDouble("commitment_score", 0.0)
        val strength = insights.optDouble("strength_progress_pct", 0.0)
        val weight = insights.optDouble("weight_progress_pct", 0.0)

        tvIntensity.text = "${"%.1f".format(intensity)}/10"
        tvCommitment.text = "${"%.1f".format(commitment)}/10"
        progressIntensity.progress = ((intensity / 10.0) * 100).toInt().coerceIn(0, 100)
        progressCommitment.progress = ((commitment / 10.0) * 100).toInt().coerceIn(0, 100)

        tvStrengthPct.text = "${"%.0f".format(strength)}%"
        tvWeightPct.text = "${"%.0f".format(weight)}%"
        progressStrength.progress = strength.toInt().coerceIn(0, 100)
        progressWeight.progress = weight.toInt().coerceIn(0, 100)

        tvSuggestion.text = insights.optString("ai_suggestion", "No insight available")
        tvCalories.text = insights.optInt("suggested_calories", 0).toString()
    }
}
