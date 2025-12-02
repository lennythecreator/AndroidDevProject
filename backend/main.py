from models import *
from typing import Union

from fastapi import FastAPI
app = FastAPI()


@app.get("/")
def read_root():
    return {"Hello": "World"}

@app.post("/login")
async def login(login_request: LoginRequest):
    return await Auth_Controller.login(login_request)

@app.post("/onboarding")
async def onboard(onboarding_request: OnboardingRequest):
    return await User_Controller.onboard(onboarding_request)

@app.get("/users/{id}/averages")
await def user_averages(id: int):
    return await User_Controller.get_goal_averages(id)

@app.get("/users/{id}/goals/current")
async def user_current_goals(id: int):
    return await User_Controller.get_current_goals(id)

@app.get("/users/{id}/insights")
async def user_insights(id: int):
    return await User_Controller.get_insights(id)
@app.get("/items/{item_id}")
def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}

