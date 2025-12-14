from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from controllers.auth import Auth_Controller
from controllers.user import User_Controller
from db import init_db
from models import (
    ChatMessageRequest,
    LoginRequest,
    OnboardingRequest,
    SignupRequest,
    WorkoutRequest,
)

app = FastAPI()

# Add CORS middleware to allow requests from Android app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, replace with specific origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def startup_event() -> None:
    init_db()


@app.get("/")
def read_root():
    return {"status": "ok"}


@app.post("/signup")
async def signup(signup_request: SignupRequest):
    return await Auth_Controller.signup(signup_request)


@app.post("/login")
async def login(login_request: LoginRequest):
    return await Auth_Controller.login(login_request)


@app.post("/onboarding")
async def onboard(onboarding_request: OnboardingRequest):
    return await User_Controller.onboard(onboarding_request)


@app.post("/workouts")
async def save_workout(workout_request: WorkoutRequest):
    return await User_Controller.save_workout(workout_request)


@app.get("/users/{user_id}/profile")
async def user_profile(user_id: int):
    return await User_Controller.get_profile(user_id)


@app.get("/users/{user_id}/averages")
async def user_averages(user_id: int):
    return await User_Controller.get_goal_averages(user_id)


@app.get("/users/{user_id}/goals/current")
async def user_current_goals(user_id: int):
    return await User_Controller.get_current_goals(user_id)


@app.get("/users/{user_id}/insights")
async def user_insights(user_id: int):
    return await User_Controller.get_insights(user_id)


@app.get("/users/{user_id}/insights/chat")
async def get_chat_history(user_id: int):
    return await User_Controller.get_chat_history(user_id)


@app.post("/users/{user_id}/insights/chat")
async def send_chat_message(user_id: int, chat_request: ChatMessageRequest):
    return await User_Controller.send_chat_message(user_id, chat_request.message)
