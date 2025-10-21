@echo off
echo =====================================
echo Payroll Management System - Debug Mode
echo =====================================
echo.

echo Creating debug directories...
if not exist "logs" mkdir logs
if not exist "debug" mkdir debug

echo.
echo Starting services in DEBUG mode...
echo - Application Port: 20001
echo - Debug Port: 5005 (for remote debugging)
echo - Database Port: 5432
echo - PgAdmin: http://localhost:5050
echo.

echo To connect remote debugger:
echo 1. Open your IDE (IntelliJ/Eclipse/VS Code)
echo 2. Create remote debug configuration
echo 3. Host: localhost, Port: 5005
echo 4. Start debugging session
echo.

echo Starting Docker containers...
docker-compose -f docker-compose.yml -f docker-compose.debug.yml up --build

echo.
echo Debug session ended.
pause