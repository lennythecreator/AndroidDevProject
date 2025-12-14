# GymPal Backend

FastAPI backend server for the GymPal fitness tracking application.

## Prerequisites

- Python 3.12 or higher
- [uv](https://github.com/astral-sh/uv) (recommended) or pip

## Setup

### 1. Virtual Environment Setup

**Quick Setup (Recommended):**

Run the setup script for your platform:

**Windows (PowerShell):**
```powershell
cd backend
.\setup.ps1
```

**macOS/Linux:**
```bash
cd backend
chmod +x setup.sh
./setup.sh
```

**Manual Setup:**

**Using uv (recommended):**

`uv` automatically creates and manages a virtual environment. Just run:

```bash
cd backend
uv sync
```

This will:
- Create a `.venv` directory (virtual environment)
- Install all dependencies from `pyproject.toml`
- Lock dependencies in `uv.lock`

To activate the virtual environment manually (if needed):
```bash
# On Windows (PowerShell)
.\.venv\Scripts\Activate.ps1

# On Windows (CMD)
.venv\Scripts\activate.bat

# On macOS/Linux
source .venv/bin/activate
```

**Using Python venv (alternative):**

If you prefer using standard Python venv:

```bash
cd backend
python -m venv .venv

# Activate virtual environment
# On Windows (PowerShell)
.\.venv\Scripts\Activate.ps1

# On Windows (CMD)
.venv\Scripts\activate.bat

# On macOS/Linux
source .venv/bin/activate

# Install dependencies
pip install fastapi uvicorn pydantic groq python-dotenv
```

### 2. Environment Variables

Create a `.env` file in the `backend` directory with your Groq API key:

```env
GROQ_API_KEY=your_groq_api_key_here
```

To get a Groq API key:
1. Visit https://console.groq.com/
2. Sign up or log in
3. Create an API key
4. Add it to your `.env` file

### 3. Database

The database will be automatically created in `backend/data/gympal.db` when you first run the server. The `data` directory already exists.

## Running the Server

### Option 1: Using uv

```bash
cd backend
uv run uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Option 2: Using Python directly (with activated venv)

```bash
cd backend
# Make sure virtual environment is activated first
# Then run:
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Or if using `uv` without activation:
```bash
cd backend
uv run uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Option 3: Using Docker

```bash
cd backend
docker-compose up --build
```

The server will start on `http://localhost:8000`

## API Documentation

Once the server is running, you can access:
- **Interactive API docs (Swagger UI):** http://localhost:8000/docs
- **Alternative API docs (ReDoc):** http://localhost:8000/redoc

## Endpoints

- `GET /` - Health check
- `POST /signup` - User registration
- `POST /login` - User authentication
- `POST /onboarding` - Complete user onboarding
- `POST /workouts` - Save a workout
- `GET /users/{user_id}/profile` - Get user profile
- `GET /users/{user_id}/averages` - Get workout averages
- `GET /users/{user_id}/goals/current` - Get current goals
- `GET /users/{user_id}/insights` - Get AI-generated insights
- `GET /users/{user_id}/insights/chat` - Get chat history
- `POST /users/{user_id}/insights/chat` - Send chat message

## Notes

- The database is SQLite and stored in `backend/data/gympal.db`
- The AI features require a valid `GROQ_API_KEY` in the `.env` file
- If the API key is missing, insights will fall back to default values
- The server runs with auto-reload enabled for development
- The virtual environment (`.venv`) is automatically created by `uv sync` and should be added to `.gitignore`

