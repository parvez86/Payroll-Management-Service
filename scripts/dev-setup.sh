#!/bin/bash

# Payroll Management System - Development Setup Script

echo "🚀 Starting Payroll Management System Development Environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Pull latest images
echo "📥 Pulling latest Docker images..."
docker-compose pull

# Build and start services
echo "🏗️ Building and starting services..."
docker-compose --profile dev up --build -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check service health
echo "🏥 Checking service health..."
docker-compose ps

# Show useful URLs
echo ""
echo "✅ Development environment is ready!"
echo ""
echo "🔗 Useful URLs:"
echo "   • Backend API: http://localhost:8080/api"
echo "   • Health Check: http://localhost:8080/api/actuator/health"
echo "   • H2 Console (if dev profile): http://localhost:8080/api/h2-console"
echo "   • pgAdmin: http://localhost:5050 (admin@payroll.com / admin123)"
echo "   • PostgreSQL: localhost:5432 (payroll_user / payroll_pass)"
echo ""
echo "📝 To view logs:"
echo "   docker-compose logs -f payroll-service"
echo ""
echo "🛑 To stop services:"
echo "   docker-compose down"
echo ""