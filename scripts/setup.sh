#!/bin/bash
# Payroll Management System - Initial Setup Script for Linux/Mac

echo ""
echo "============================================"
echo "Payroll Management System - Initial Setup"
echo "============================================"
echo ""

# Check if .env already exists
if [ -f ".env" ]; then
    echo "ℹ️  .env file already exists"
else
    echo "📝 Creating .env from .env.example..."
    if cp .env.example .env; then
        echo "✅ .env created successfully"
    else
        echo "❌ Failed to create .env"
        exit 1
    fi
fi

# Check if Docker is running
echo ""
echo "🐳 Checking Docker..."
if docker info > /dev/null 2>&1; then
    echo "✅ Docker is running"
else
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start services
echo ""
echo "🚀 Starting services..."
docker-compose up -d

# Wait for services to be ready
echo ""
echo "⏳ Waiting for services to initialize (30 seconds)..."
sleep 30

# Check service health
echo ""
echo "🏥 Checking service health..."
docker-compose ps

# Display helpful information
echo ""
echo "============================================"
echo "✅ Setup Complete!"
echo "============================================"
echo ""
echo "🌐 Access the application:"
echo "    • Backend API: http://localhost:20001/pms/v1/api"
echo "    • Swagger UI: http://localhost:20001/pms/v1/api/swagger-ui/index.html"
echo "    • Health: http://localhost:20001/pms/v1/api/actuator/health"
echo ""
echo "🗄️  Database:"
echo "    • PgAdmin: http://localhost:5050"
echo "    • Email: admin@payroll.com"
echo "    • Password: admin123"
echo ""
echo "📝 Useful commands:"
echo "    • view logs: docker-compose logs -f payroll-backend"
echo "    • stop services: docker-compose down"
echo "    • rebuild: docker-compose up -d --build"
echo ""
