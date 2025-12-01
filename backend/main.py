from typing import Union

from fastapi import FastAPI

app = FastAPI()


@app.get("/")
def read_root():
    return {"Hello": "World"}

@app.post("/login")
def login(login_request: LoginRequest):
    return Auth_Controller.login(login_request)

@app.post("/onboarding")
def onboard(onboarding_request: OnboardingRequest):
    return User_Controller.onboard(onboarding_request)

@app.get("/users/{id}/averages")
def user_averages(id: int):
    return User_Controller.get_user_averages(id)

@app.get("/users/{id}/goals/current")
def user_current_goals(id: int):
    return User_Controller.get_current_goals(id)

@app.get("/items/{item_id}")
def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}

