import os
import json
import re
from typing import Any, Dict, List

from dotenv import load_dotenv
from groq import Groq

load_dotenv()


class InsightsLLM:
    def __init__(self, api_key: str | None = None):
        self.client = Groq(api_key=api_key or os.environ.get("GROQ_API_KEY"))
        if not self.client.api_key:
            raise RuntimeError("GROQ_API_KEY not set")

    def build_prompt(self, profile: Dict[str, Any], workouts: Dict[str, Any]) -> str:
        return (
            "You are a fitness coach. Summarize and evaluate the user's profile, "
            "recent workouts, and goals, then return ONLY a JSON object matching "
            "this schema (no extra text):\n"
            "{\n"
            '  "intensity_score": float (0-10),\n'
            '  "commitment_score": float (0-10),\n'
            '  "strength_progress_pct": float (0-100),\n'
            '  "weight_progress_pct": float (0-100),\n'
            '  "ai_suggestion": "short actionable recommendation",\n'
            '  "suggested_calories": int,\n'
            '  "weekly_summary": {\n'
            '     "workouts": number,\n'
            '     "avg_sets": number,\n'
            '     "avg_reps": number,\n'
            '     "avg_duration_minutes": number,\n'
            '     "recent_workouts": [...],\n'
            '     "user": {"name": "...", "activeness": "...", "goals": "..."}\n'
            "  }\n"
            "}\n"
            "Input data:\n"
            f"Profile: {profile}\n"
            f"Workouts/goals: {workouts}\n"
        )

    def generate_insights(self, profile: Dict[str, Any], workouts: Dict[str, Any]) -> Dict[str, Any]:
        prompt = self.build_prompt(profile, workouts)
        completion = self.client.chat.completions.create(
            messages=[{"role": "user", "content": prompt}],
            model="llama-3.3-70b-versatile",
            temperature=0.2,
            max_tokens=400,
        )
        content = completion.choices[0].message.content or "{}"
        # We expect valid JSON per instructions; best-effort parse
        return _parse_json_content(content)

    def chat_with_context(
        self,
        profile: Dict[str, Any],
        workouts: Dict[str, Any],
        insights: Dict[str, Any],
        chat_history: List[Dict[str, str]],
    ) -> str:
        """
        Generate an AI response in a conversational context with full user data.
        
        Args:
            profile: User profile data
            workouts: Workout data including averages and recent workouts
            insights: Current insights data
            chat_history: List of previous messages in format [{"role": "user|assistant", "content": "..."}]
        
        Returns:
            AI response as a string
        """
        system_prompt = (
            "You are a helpful fitness coach assistant. You have access to the user's complete "
            "fitness profile, workout history, and current insights. Answer their questions about "
            "their fitness journey, provide personalized advice, and help them understand their "
            "progress. Be conversational, supportive, and actionable.\n\n"
            f"User Profile: {profile}\n"
            f"Workout Data: {workouts}\n"
            f"Current Insights: {insights}\n\n"
            "Use this context to provide relevant, personalized responses."
        )

        messages = [{"role": "system", "content": system_prompt}]
        
        # Add chat history
        for msg in chat_history:
            messages.append({"role": msg["role"], "content": msg["content"]})

        completion = self.client.chat.completions.create(
            messages=messages,
            model="llama-3.3-70b-versatile",
            temperature=0.7,
            max_tokens=500,
        )
        
        return completion.choices[0].message.content or "I'm sorry, I couldn't generate a response."


def _parse_json_content(content: str) -> Dict[str, Any]:
    try:
        return json.loads(content)
    except Exception:
        pass

    fenced = re.search(r"```(?:json)?(.*)```", content, re.DOTALL | re.IGNORECASE)
    if fenced:
        try:
            return json.loads(fenced.group(1).strip())
        except Exception:
            pass

    curly = re.search(r"(\{.*\})", content, re.DOTALL)
    if curly:
        try:
            return json.loads(curly.group(1))
        except Exception:
            pass
    return {}
