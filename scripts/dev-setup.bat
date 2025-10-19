@echo off
REM Payroll Management System - Development Setup Script for Windows

echo 🚀 Starting Payroll Management System Development Environment...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not running. Please start Docker first.
    exit /b 1
)

REM Pull latest images
echo 📥 Pulling latest Docker images...
docker-compose pull

REM Build and start services
echo 🏗️ Building and starting services...
docker-compose --profile dev up --build -d

REM Wait for services to be ready
echo ⏳ Waiting for services to be ready...
timeout /t 30 /nobreak

REM Check service health
echo 🏥 Checking service health...
docker-compose ps

REM Show useful URLs
echo.
echo ✅ Development environment is ready!
echo.
echo 🔗 Useful URLs:
echo    • Backend API: http://localhost:8080/api
echo    • Health Check: http://localhost:8080/api/actuator/health
echo    • H2 Console (if dev profile): http://localhost:8080/api/h2-console
echo    • pgAdmin: http://localhost:5050 (admin@payroll.com / admin123)
echo    • PostgreSQL: localhost:5432 (payroll_user / payroll_pass)
echo.
echo 📝 To view logs:
echo    docker-compose logs -f payroll-service
echo.
echo 🛑 To stop services:
echo    docker-compose down
echo.