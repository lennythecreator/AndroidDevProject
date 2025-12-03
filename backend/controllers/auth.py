import hashlib
import sqlite3

from fastapi import HTTPException

from db import get_connection
from models import LoginRequest, SignupRequest


def _hash_password(password: str) -> str:
    return hashlib.sha256(password.encode("utf-8")).hexdigest()


class Auth_Controller:
    @staticmethod
    async def signup(signup_request: SignupRequest) -> dict:
        conn = get_connection()
        try:
            with conn:
                conn.execute(
                    "INSERT INTO users (email, password_hash) VALUES (?, ?)",
                    (signup_request.email, _hash_password(signup_request.password)),
                )
        except sqlite3.IntegrityError as exc:
            conn.close()
            raise HTTPException(
                status_code=400, detail="Email already registered"
            ) from exc

        user_row = conn.execute(
            "SELECT id FROM users WHERE email = ?",
            (signup_request.email,),
        ).fetchone()
        conn.close()
        return {"user_id": user_row["id"], "email": signup_request.email}

    @staticmethod
    async def login(login_request: LoginRequest) -> dict:
        conn = get_connection()
        row = conn.execute(
            "SELECT id, password_hash FROM users WHERE email = ?",
            (login_request.email,),
        ).fetchone()
        if not row:
            conn.close()
            raise HTTPException(status_code=404, detail="User not found")

        if row["password_hash"] != _hash_password(login_request.password):
            conn.close()
            raise HTTPException(status_code=401, detail="Invalid credentials")

        conn.close()
        return {"user_id": row["id"], "email": login_request.email}
