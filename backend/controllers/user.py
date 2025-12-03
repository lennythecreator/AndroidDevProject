from typing import List

from fastapi import HTTPException

from db import get_connection
from models import (
    AveragesResponse,
    GoalsResponse,
    InsightResponse,
    OnboardingRequest,
    ProfileResponse,
    WorkoutRequest,
)


class User_Controller:
    @staticmethod
    async def onboard(onboarding_request: OnboardingRequest) -> dict:
        conn = get_connection()
        try:
            with conn:
                user_row = conn.execute(
                    "SELECT id FROM users WHERE id = ?",
                    (onboarding_request.user_id,),
                ).fetchone()
                if not user_row:
                    raise HTTPException(status_code=404, detail="User not found")

                conn.execute(
                    """
                    UPDATE users
                    SET name = ?, age = ?, weight_kg = ?, height_cm = ?, activeness = ?,
                        fitness_goals = ?, onboarding_complete = 1
                    WHERE id = ?
                    """,
                    (
                        onboarding_request.name,
                        onboarding_request.age,
                        onboarding_request.weight_kg,
                        onboarding_request.height_cm,
                        onboarding_request.activeness,
                        onboarding_request.fitness_goals,
                        onboarding_request.user_id,
                    ),
                )

                goals = onboarding_request.goals
                if goals is None and onboarding_request.fitness_goals:
                    goals = _split_goals(onboarding_request.fitness_goals)

                if goals:
                    conn.execute(
                        "DELETE FROM goals WHERE user_id = ? AND active = 1",
                        (onboarding_request.user_id,),
                    )
                    for goal in goals:
                        conn.execute(
                            "INSERT INTO goals (user_id, description, active) VALUES (?, ?, 1)",
                            (onboarding_request.user_id, goal),
                        )
        finally:
            conn.close()
        return {"message": "onboarding saved", "user_id": onboarding_request.user_id}

    @staticmethod
    async def save_workout(workout_request: WorkoutRequest) -> dict:
        if not workout_request.exercises:
            raise HTTPException(
                status_code=400, detail="Add at least one exercise to save a workout"
            )

        conn = get_connection()
        try:
            with conn:
                user_row = conn.execute(
                    "SELECT id FROM users WHERE id = ?",
                    (workout_request.user_id,),
                ).fetchone()
                if not user_row:
                    raise HTTPException(status_code=404, detail="User not found")

                conn.execute(
                    "INSERT INTO workouts (user_id, day) VALUES (?, ?)",
                    (workout_request.user_id, workout_request.day),
                )
                workout_id = conn.execute("SELECT last_insert_rowid()").fetchone()[0]

                for exercise in workout_request.exercises:
                    conn.execute(
                        """
                        INSERT INTO exercises (workout_id, name, reps, sets, duration)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        (
                            workout_id,
                            exercise.name,
                            exercise.reps,
                            exercise.sets,
                            exercise.duration,
                        ),
                    )
        finally:
            conn.close()
        return {"message": "workout saved", "workout_id": workout_id}

    @staticmethod
    async def get_goal_averages(user_id: int) -> AveragesResponse:
        conn = get_connection()
        try:
            exists = conn.execute(
                "SELECT 1 FROM users WHERE id = ?",
                (user_id,),
            ).fetchone()
            if not exists:
                raise HTTPException(status_code=404, detail="User not found")

            workouts_week = conn.execute(
                """
                SELECT COUNT(*) AS count
                FROM workouts
                WHERE user_id = ? AND created_at >= datetime('now', '-7 day')
                """,
                (user_id,),
            ).fetchone()["count"]

            exercise_totals = conn.execute(
                """
                SELECT
                    SUM(e.sets) AS total_sets,
                    SUM(e.reps) AS total_reps,
                    SUM(e.duration) AS total_duration
                FROM exercises e
                JOIN workouts w ON e.workout_id = w.id
                WHERE w.user_id = ? AND w.created_at >= datetime('now', '-7 day')
                """,
                (user_id,),
            ).fetchone()
        finally:
            conn.close()

        workouts_for_avg = workouts_week if workouts_week else 1
        avg_sets = (exercise_totals["total_sets"] or 0) / workouts_for_avg
        avg_reps = (exercise_totals["total_reps"] or 0) / workouts_for_avg
        avg_duration = (exercise_totals["total_duration"] or 0) / workouts_for_avg

        return AveragesResponse(
            workouts_per_week=float(workouts_week),
            avg_sets=round(avg_sets, 2),
            avg_reps=round(avg_reps, 2),
            avg_duration_minutes=round(avg_duration, 2),
        )

    @staticmethod
    async def get_current_goals(user_id: int) -> GoalsResponse:
        conn = get_connection()
        try:
            exists = conn.execute(
                "SELECT 1 FROM users WHERE id = ?",
                (user_id,),
            ).fetchone()
            if not exists:
                raise HTTPException(status_code=404, detail="User not found")

            rows = conn.execute(
                """
                SELECT description FROM goals
                WHERE user_id = ? AND active = 1
                ORDER BY created_at DESC
                """,
                (user_id,),
            ).fetchall()

            if rows:
                goals = [row["description"] for row in rows]
            else:
                # Fallback to the free-form fitness goals on the user record
                fitness_goals = conn.execute(
                    "SELECT fitness_goals FROM users WHERE id = ?",
                    (user_id,),
                ).fetchone()
                goals = (
                    _split_goals(fitness_goals["fitness_goals"]) if fitness_goals else []
                )
        finally:
            conn.close()
        return GoalsResponse(goals=goals)

    @staticmethod
    async def get_insights(user_id: int) -> InsightResponse:
        # Pull real user + weekly averages from DB, but return a mocked insight payload.
        profile = await User_Controller.get_profile(user_id)
        averages = await User_Controller.get_goal_averages(user_id)
        recent_workouts = _fetch_recent_workouts(user_id)

        return InsightResponse(
            intensity_score=8.0,
            commitment_score=9.0,
            strength_progress_pct=72.0,
            weight_progress_pct=45.0,
            ai_suggestion=(
                "Solid consistency—bump compound lifts by 5% next session and "
                "keep rest under 90s to push strength gains."
            ),
            suggested_calories=2450,
            weekly_summary={
                "workouts": averages.workouts_per_week,
                "avg_sets": averages.avg_sets,
                "avg_reps": averages.avg_reps,
                "avg_duration_minutes": averages.avg_duration_minutes,
                "recent_workouts": recent_workouts,
                "user": {
                    "name": profile.name,
                    "activeness": profile.activeness,
                    "goals": profile.fitness_goals,
                },
            },
        )

    @staticmethod
    async def get_profile(user_id: int) -> ProfileResponse:
        conn = get_connection()
        try:
            row = conn.execute(
                """
                SELECT id, email, name, age, weight_kg, height_cm, activeness,
                       fitness_goals, onboarding_complete
                FROM users
                WHERE id = ?
                """,
                (user_id,),
            ).fetchone()
        finally:
            conn.close()

        if not row:
            raise HTTPException(status_code=404, detail="User not found")

        return ProfileResponse(
            user_id=row["id"],
            email=row["email"],
            name=row["name"],
            age=row["age"],
            weight_kg=row["weight_kg"],
            height_cm=row["height_cm"],
            activeness=row["activeness"],
            fitness_goals=row["fitness_goals"],
            onboarding_complete=bool(row["onboarding_complete"]),
        )


def _split_goals(raw_goals: str) -> List[str]:
    if not raw_goals:
        return []
    parts = []
    for chunk in raw_goals.replace(";", ",").split(","):
        cleaned = chunk.strip()
        if cleaned:
            parts.append(cleaned)
    return parts


def _estimate_calories(weight_kg: float, height_cm: float, age: int, activeness: str) -> float:
    # Simple Mifflin-St Jeor with a light activity multiplier
    if age <= 0:
        age = 25
    multiplier = 1.3
    activeness_lower = activeness.lower()
    if "very" in activeness_lower:
        multiplier = 1.725
    elif "active" in activeness_lower:
        multiplier = 1.55
    elif "light" in activeness_lower:
        multiplier = 1.375
    elif "sedentary" in activeness_lower:
        multiplier = 1.2

    bmr = (10 * weight_kg) + (6.25 * height_cm) - (5 * age) + 5
    if bmr <= 0:
        bmr = 1600
    return bmr * multiplier


def _build_suggestion(commitment_score: float, strength_progress_pct: float) -> str:
    if commitment_score >= 8 and strength_progress_pct >= 70:
        return "Great consistency this week. Add a 5% load increase on compound lifts to keep progress steady."
    if commitment_score < 5:
        return "Aim for at least 3 sessions this week. Shorter 30 minute workouts still move you forward."
    return "Solid effort. Keep rest periods tight and add one extra set to your main lifts for better strength gains."


def _fetch_recent_workouts(user_id: int, limit: int = 10) -> list:
    conn = get_connection()
    try:
        rows = conn.execute(
            """
            SELECT w.id, w.day, w.created_at,
                   COUNT(e.id) AS exercise_count,
                   SUM(e.sets) AS total_sets,
                   SUM(e.reps) AS total_reps,
                   SUM(e.duration) AS total_duration
            FROM workouts w
            LEFT JOIN exercises e ON e.workout_id = w.id
            WHERE w.user_id = ?
            GROUP BY w.id
            ORDER BY w.created_at DESC
            LIMIT ?
            """,
            (user_id, limit),
        ).fetchall()
        return [
            {
                "workout_id": row["id"],
                "day": row["day"],
                "created_at": row["created_at"],
                "exercise_count": row["exercise_count"],
                "total_sets": row["total_sets"] or 0,
                "total_reps": row["total_reps"] or 0,
                "total_duration": row["total_duration"] or 0,
            }
            for row in rows
        ]
    finally:
        conn.close()
