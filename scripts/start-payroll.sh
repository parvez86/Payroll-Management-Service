#!/bin/bash

# Payroll Management System - Startup Script
echo "🚀 Starting Payroll Management System..."
echo "🚀 ==========================================="

# Function to print URLs after startup
print_urls() {
    echo ""
    echo "🚀 ==========================================="
    echo "🚀 Payroll Management System - URLs"
    echo "🚀 ==========================================="
    echo "🌐 Application: http://localhost:20001/pms/v1/api"
    echo "📚 Swagger UI: http://localhost:20001/pms/v1/api/swagger-ui/index.html"
    echo "📋 API Docs: http://localhost:20001/pms/v1/api/v3/api-docs"
    echo "❤️  Health: http://localhost:20001/pms/v1/api/actuator/health"
    echo "🔐 Login: POST http://localhost:20001/pms/v1/api/auth/login"
    echo "🗄️  Database: PostgreSQL on localhost:5432"
    echo "🗄️  PgAdmin: http://localhost:5050 (admin@payroll.com / admin123)"
    echo "🚀 ==========================================="
    echo ""
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start the application
echo "🔧 Building and starting containers..."
docker-compose up --build -d

# Wait for services to be healthy
echo "⏳ Waiting for services to start..."
sleep 10

# Check if payroll service is running
if docker-compose ps | grep -q "payroll-backend.*Up"; then
    echo "✅ Payroll service is running!"
    print_urls
    
    # Follow logs to show startup messages
    echo "📋 Following startup logs (Ctrl+C to stop):"
    echo "---"
    docker-compose logs -f payroll-service
else
    echo "❌ Failed to start payroll service"
    echo "📋 Checking logs..."
    docker-compose logs payroll-service
    exit 1
fi