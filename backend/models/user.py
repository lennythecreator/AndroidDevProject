from typing import List, Optional

from pydantic import BaseModel, Field


class SignupRequest(BaseModel):
    email: str
    password: str


class LoginRequest(BaseModel):
    email: str
    password: str


class OnboardingRequest(BaseModel):
    user_id: int
    name: str
    age: int = 0
    weight_kg: float = 0
    height_cm: float = 0
    activeness: str = ""
    fitness_goals: str = ""
    goals: Optional[List[str]] = None


class ExerciseInput(BaseModel):
    name: str
    reps: int = 0
    sets: int = 0
    duration: int = 0


class WorkoutRequest(BaseModel):
    user_id: int
    day: str
    exercises: List[ExerciseInput] = Field(default_factory=list)


class AveragesResponse(BaseModel):
    workouts_per_week: float
    avg_sets: float
    avg_reps: float
    avg_duration_minutes: float


class GoalsResponse(BaseModel):
    goals: List[str]


class InsightResponse(BaseModel):
    intensity_score: float
    commitment_score: float
    strength_progress_pct: float
    weight_progress_pct: float
    ai_suggestion: str
    suggested_calories: int
    weekly_summary: dict


class ProfileResponse(BaseModel):
    user_id: int
    email: str
    name: str
    age: int
    weight_kg: float
    height_cm: float
    activeness: str
    fitness_goals: str
    onboarding_complete: bool
