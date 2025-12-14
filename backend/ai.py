import os
import json
import re
from typing import Any, Dict

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
