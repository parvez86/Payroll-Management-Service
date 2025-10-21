@echo off
REM Payroll Management System - Startup Script for Windows

echo 🚀 Starting Payroll Management System...
echo 🚀 ===========================================

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not running. Please start Docker first.
    pause
    exit /b 1
)

REM Start the application
echo 🔧 Building and starting containers...
docker-compose up --build -d

REM Wait for services to start
echo ⏳ Waiting for services to start...
timeout /t 10 /nobreak >nul

REM Check if payroll service is running
docker-compose ps | findstr "payroll-backend" | findstr "Up" >nul
if %errorlevel% equ 0 (
    echo ✅ Payroll service is running!
    echo.
    echo 🚀 ===========================================
    echo 🚀 Payroll Management System - URLs
    echo 🚀 ===========================================
    echo 🌐 Application: http://localhost:20001/pms/v1/api
    echo 📚 Swagger UI: http://localhost:20001/pms/v1/api/swagger-ui/index.html
    echo 📋 API Docs: http://localhost:20001/pms/v1/api/v3/api-docs
    echo ❤️  Health: http://localhost:20001/pms/v1/api/actuator/health
    echo 🔐 Login: POST http://localhost:20001/pms/v1/api/auth/login
    echo 🗄️  Database: PostgreSQL on localhost:5432
    echo 🗄️  PgAdmin: http://localhost:5050 ^(admin@payroll.com / admin123^)
    echo 🚀 ===========================================
    echo.
    echo 📋 Following startup logs ^(Ctrl+C to stop^):
    echo ---
    docker-compose logs -f payroll-service
) else (
    echo ❌ Failed to start payroll service
    echo 📋 Checking logs...
    docker-compose logs payroll-service
    pause
    exit /b 1
)