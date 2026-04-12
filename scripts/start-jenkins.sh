#!/bin/bash
# ============================================================
# Jenkins Startup Script (Linux/macOS)
# ============================================================
# This script starts Jenkins with the proper environment configuration.
# Jenkins runs on port 8080 (configurable via .env)
# Will NOT start the main Payroll Service (use start-payroll.sh for that)
# ============================================================

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Navigate to project root
cd "$(dirname "$0")/.." || exit 1

# Check if Docker is running
echo "Checking Docker status..."
if ! docker ps > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check if .env exists, if not create it from .env.example
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  .env file not found. Creating from .env.example...${NC}"
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${GREEN}✅ .env file created successfully${NC}"
    else
        echo -e "${RED}❌ .env.example not found. Cannot proceed.${NC}"
        exit 1
    fi
fi

# Start Jenkins services
echo ""
echo -e "${GREEN}🚀 Starting Jenkins services...${NC}"
echo ""

docker-compose -f jenkins/docker-compose.yml --env-file .env up -d

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to start Jenkins${NC}"
    exit 1
fi

# Wait for services to initialize
echo "⏳ Waiting for services to initialize (30 seconds)..."
sleep 30

# Display service status
echo ""
echo -e "${GREEN}📊 SERVICE STATUS:${NC}"
echo ""
docker-compose -f jenkins/docker-compose.yml --env-file .env ps --format "table {{.Service}}\t{{.Status}}"

echo ""
echo -e "${GREEN}✅ JENKINS STARTED SUCCESSFULLY!${NC}"
echo ""
echo "🔗 Access Jenkins:"
echo "   🌐 Jenkins UI: http://localhost:8080"
echo "   📊 PgAdmin: http://localhost:5050 (check .env for credentials)"
echo ""
echo "💡 TIP: Jenkins might take a few minutes to fully initialize on first start."
echo "   The health check will show 'health: starting' initially."
echo ""
echo "To view Jenkins logs, run:"
echo "   docker-compose -f jenkins/docker-compose.yml logs jenkins"
echo ""
echo "To stop Jenkins, run:"
echo "   docker-compose -f jenkins/docker-compose.yml down"
echo ""
