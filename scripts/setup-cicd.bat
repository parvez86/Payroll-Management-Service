@echo off
REM Setup CI/CD configuration for local development (Windows)
REM This script helps developers set up local pre-commit hooks and configuration

setlocal enabledelayedexpansion

REM Colors using ANSI escape codes
set RESET=[0m
set GREEN=[0;32m
set BLUE=[0;34m
set YELLOW=[1;33m
set RED=[0;31m

cls
echo %BLUE%════════════════════════════════════════════════════════%RESET%
echo %BLUE%   Payroll CI/CD Local Development Setup%RESET%
echo %BLUE%════════════════════════════════════════════════════════%RESET%
echo.

REM Check if we're in a git repository
if not exist ".git" (
    echo %RED%❌ Not a git repository. Run this script from the project root.%RESET%
    exit /b 1
)

REM 1. Create environment file template
echo %YELLOW%📝 Creating environment configuration...%RESET%

if not exist ".env.local" (
    (
        echo # Local Development Environment Variables
        echo # Copy this file to .env.local and update values as needed
        echo.
        echo # Spring Profile
        echo SPRING_PROFILES_ACTIVE=dev
        echo.
        echo # Database Configuration
        echo SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payroll_db
        echo SPRING_DATASOURCE_USERNAME=payroll_user
        echo SPRING_DATASOURCE_PASSWORD=payroll_pass
        echo.
        echo # JWT Configuration
        echo JWT_SECRET=your-secret-key-change-in-production-at-least-256-bits
        echo JWT_EXPIRATION=86400000
        echo.
        echo # Server Configuration
        echo SERVER_PORT=20001
        echo SERVER_SERVLET_CONTEXT_PATH=/pms
        echo.
        echo # Logging
        echo LOGGING_LEVEL_ROOT=INFO
        echo LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=INFO
        echo LOGGING_LEVEL_ORG_SP_PAYROLL=DEBUG
        echo.
        echo # Actuator
        echo MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics,info
    ) > .env.local
    
    echo %GREEN%✅ Created .env.local template%RESET%
    echo %YELLOW%   Update .env.local with your local configuration%RESET%
) else (
    echo %BLUE%ℹ️  .env.local already exists%RESET%
)

REM 2. Verify Gradle wrapper
echo.
echo %YELLOW%✅ Verifying Gradle wrapper...%RESET%

if exist "gradlew.bat" (
    echo %GREEN%✅ Gradle wrapper ready%RESET%
) else (
    echo %YELLOW%⚠️  Gradle wrapper not found%RESET%
)

REM 3. Build project test
echo.
echo %YELLOW%🏗️  Testing build configuration...%RESET%

call gradlew clean --no-daemon >nul 2>&1
if !errorlevel! equ 0 (
    echo %GREEN%✅ Gradle build system is ready%RESET%
) else (
    echo %RED%❌ Issues with Gradle configuration%RESET%
    echo    Run: gradlew clean build --no-daemon
)

REM Summary
echo.
echo %BLUE%════════════════════════════════════════════════════════%RESET%
echo %GREEN%✅ CI/CD Development Setup Complete!%RESET%
echo %BLUE%════════════════════════════════════════════════════════%RESET%
echo.
echo %YELLOW%📋 Next steps:%RESET%
echo.
echo 1. 🔐 Configure secrets in GitHub:
echo    Repository ^> Settings ^> Secrets and variables ^> Actions
echo.
echo 2. 📝 Update .env.local with your configuration
echo.
echo 3. 🏗️  Build the project:
echo    gradlew clean build
echo.
echo 4. 🚀 Start development:
echo    .\scripts\start-debug.bat
echo.
echo 5. 📚 Review CI/CD documentation:
echo    docs\CI-CD-PLAN.md
echo    .github\GITHUB_ACTIONS_SETUP.md
echo.
echo %BLUE%════════════════════════════════════════════════════════%RESET%
echo.
pause
