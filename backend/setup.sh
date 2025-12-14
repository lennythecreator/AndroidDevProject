#!/bin/bash
# Bash script to set up the backend virtual environment

echo "Setting up GymPal Backend..."

# Check if uv is installed
if command -v uv &> /dev/null; then
    echo "Using uv to set up virtual environment..."
    uv sync
    echo "Virtual environment created and dependencies installed!"
    echo ""
    echo "To activate the virtual environment, run:"
    echo "  source .venv/bin/activate"
    echo ""
    echo "Or use uv run to execute commands:"
    echo "  uv run uvicorn main:app --reload"
else
    echo "uv not found. Using Python venv instead..."
    
    # Check if Python is installed
    if command -v python3 &> /dev/null; then
        echo "Creating virtual environment..."
        python3 -m venv .venv
        
        echo "Activating virtual environment..."
        source .venv/bin/activate
        
        echo "Installing dependencies..."
        pip install fastapi uvicorn pydantic groq python-dotenv
        
        echo "Setup complete! Virtual environment is activated."
    else
        echo "Error: Python 3 is not installed or not in PATH"
        exit 1
    fi
fi

echo ""
echo "Don't forget to create a .env file with your GROQ_API_KEY!"

