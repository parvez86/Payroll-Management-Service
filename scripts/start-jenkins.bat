@echo off
REM ============================================================
REM Jenkins Startup Script (Windows)
REM ============================================================
REM This script starts Jenkins with the proper environment configuration.
REM Jenkins runs on port 8080 (configurable via .env)
REM Will NOT start the main Payroll Service (use start-payroll.bat for that)
REM ============================================================

setlocal enabledelayedexpansion

REM Navigate to project root
cd /d "%~dp0\.."

REM Check if Docker is running
echo Checking Docker status...
docker ps >nul 2>&1
if !errorlevel! neq 0 (
    echo ❌ Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

REM Check if .env exists, if not create it from .env.example
if not exist .env (
    echo ⚠️  .env file not found. Creating from .env.example...
    if exist .env.example (
        copy .env.example .env >nul
        echo ✅ .env file created successfully
    ) else (
        echo ❌ .env.example not found. Cannot proceed.
        pause
        exit /b 1
    )
)

REM Start Jenkins services
echo.
echo 🚀 Starting Jenkins services...
echo.

docker-compose -f jenkins/docker-compose.yml --env-file .env up -d 2>&1

if !errorlevel! neq 0 (
    echo ❌ Failed to start Jenkins
    pause
    exit /b 1
)

REM Wait for services to initialize
echo ⏳ Waiting for services to initialize (30 seconds)...
timeout /t 30 /nobreak

REM Display service status
echo.
echo 📊 SERVICE STATUS:
echo.
docker-compose -f jenkins/docker-compose.yml --env-file .env ps --format "table {{.Service}}\t{{.Status}}"

echo.
echo ✅ JENKINS STARTED SUCCESSFULLY!
echo.
echo 🔗 Access Jenkins:
echo    🌐 Jenkins UI: http://localhost:8080
echo    📊 PgAdmin: http://localhost:5050 (check .env for credentials)
echo.
echo 💡 TIP: Jenkins might take a few minutes to fully initialize on first start.
echo    The health check will show "health: starting" initially.
echo.
echo 📖 JENKINS 5-FEATURE SETUP (Production-Grade CI/CD):
echo    After Jenkins is running, complete the setup for 5 new features:
echo    1. GitHub Webhook Triggers
echo    2. Email Notifications
echo    3. Blue Ocean UI
echo    4. GitHub Status Checks
echo    5. Branch Protection
echo.
echo    Quick start: docs\START-HERE-5-FEATURES.md (5 min)
echo    Full setup: docs\SETUP-5-FEATURES-CHECKLIST.md (45 min)
echo.
echo To view Jenkins logs, run:
echo    docker-compose -f jenkins/docker-compose.yml logs jenkins
echo.
echo To stop Jenkins, run:
echo    docker-compose -f jenkins/docker-compose.yml down
echo.
pause
