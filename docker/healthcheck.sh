#!/bin/sh
# Health check script for Docker container

# Check if the application is responding
curl -f http://localhost:20001/pms/v1/api/actuator/health || exit 1

echo "✅ Payroll service is healthy!"
echo "📚 Swagger UI: http://localhost:20001/pms/v1/api/swagger-ui/index.html"