# PowerShell script to set up the backend virtual environment

Write-Host "Setting up GymPal Backend..." -ForegroundColor Green

# Check if uv is installed
if (Get-Command uv -ErrorAction SilentlyContinue) {
    Write-Host "Using uv to set up virtual environment..." -ForegroundColor Cyan
    uv sync
    Write-Host "Virtual environment created and dependencies installed!" -ForegroundColor Green
    Write-Host "`nTo activate the virtual environment, run:" -ForegroundColor Yellow
    Write-Host "  .\.venv\Scripts\Activate.ps1" -ForegroundColor White
    Write-Host "`nOr use uv run to execute commands:" -ForegroundColor Yellow
    Write-Host "  uv run uvicorn main:app --reload" -ForegroundColor White
} else {
    Write-Host "uv not found. Using Python venv instead..." -ForegroundColor Yellow
    
    # Check if Python is installed
    if (Get-Command python -ErrorAction SilentlyContinue) {
        Write-Host "Creating virtual environment..." -ForegroundColor Cyan
        python -m venv .venv
        
        Write-Host "Activating virtual environment..." -ForegroundColor Cyan
        .\.venv\Scripts\Activate.ps1
        
        Write-Host "Installing dependencies..." -ForegroundColor Cyan
        pip install fastapi uvicorn pydantic groq python-dotenv
        
        Write-Host "Setup complete! Virtual environment is activated." -ForegroundColor Green
    } else {
        Write-Host "Error: Python is not installed or not in PATH" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`nDon't forget to create a .env file with your GROQ_API_KEY!" -ForegroundColor Yellow

