#!/bin/bash

# Payroll Management System - Startup Script

echo ""
echo "============================================"
echo "Starting Payroll Management System..."
echo "============================================"
echo ""

# Check if .env exists, create from example if not
if [ ! -f ".env" ]; then
    echo "📝 .env not found, creating from .env.example..."
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo "✅ .env created"
    else
        echo "❌ .env.example not found"
        exit 1
    fi
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi
echo "✅ Docker is running"
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