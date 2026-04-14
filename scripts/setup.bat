@echo off
REM Payroll Management System - Initial Setup Script for Windows
REM This script initializes the development environment

chcp 65001 >nul
echo.
echo ============================================
echo Payroll Management System - Initial Setup
echo ============================================
echo.

REM Check if .env already exists
if exist .env (
    echo ℹ️  .env file already exists
) else (
    echo 📝 Creating .env from .env.example...
    copy .env.example .env >nul
    if %errorlevel% equ 0 (
        echo ✅ .env created successfully
    ) else (
        echo ❌ Failed to create .env
        pause
        exit /b 1
    )
)

REM Check if Docker is running
echo.
echo 🐳 Checking Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not running. Please start Docker first.
    pause
    exit /b 1
)
echo ✅ Docker is running

REM Start services
echo.
echo 🚀 Starting services...
docker-compose up -d

REM Wait for services to be ready
echo.
echo ⏳ Waiting for services to initialize ^(30 seconds^)...
timeout /t 30 /nobreak >nul

REM Check service health
echo.
echo 🏥 Checking service health...
docker-compose ps

REM Display helpful information
echo.
echo ============================================
echo ✅ Setup Complete!
echo ============================================
echo.
echo 🌐 Access the application:
echo    • Backend API: http://localhost:20001/pms/v1/api
echo    • Swagger UI: http://localhost:20001/pms/v1/api/swagger-ui/index.html
echo    • Health: http://localhost:20001/pms/v1/api/actuator/health
echo.
echo 🗄️  Database:
echo    • PgAdmin: http://localhost:5050
echo    • Email: admin@payroll.com
echo    • Password: admin123
echo.
echo 📝 Useful commands:
echo    • view logs: docker-compose logs -f payroll-backend
echo    • stop services: docker-compose down
echo    • rebuild: docker-compose up -d --build
echo.
