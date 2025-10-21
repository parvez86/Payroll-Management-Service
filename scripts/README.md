# Payroll Management System - Startup Scripts

This directory contains automated scripts to start the complete Payroll Management System with zero configuration required.

## 🚀 One-Command Startup

### Windows Users:
```bash
scripts\start-payroll.bat
```

### Linux/Mac Users:
```bash
chmod +x scripts/start-payroll.sh
./scripts/start-payroll.sh
```

## 🔧 What the Scripts Do

1. **🐳 Docker Health Check**: Verify Docker Desktop is running
2. **🔧 Build & Start**: Execute `docker-compose up --build -d`
3. **⏳ Service Initialization**: Wait for PostgreSQL and application startup  
4. **📋 Health Validation**: Check if payroll service is running properly
5. **🌐 URL Display**: Show all important application access points
6. **📊 Log Following**: Display real-time startup logs for debugging

## ✅ Complete System Includes

- **🚀 Spring Boot Application** (Java 24 + Spring Boot 3.5.6)
- **🗄️ PostgreSQL Database** with seed data (10 employees across 6 grades)
- **🗄️ PgAdmin Database UI** for database management
- **📚 Swagger API Documentation** with JWT security integration
- **🔒 Pre-configured Security** with default admin and employee accounts

## 🌐 Access URLs (After Successful Startup)

### Application Endpoints
- **🚀 REST API Base**: http://localhost:20001/pms/v1/api
- **📚 Swagger UI**: http://localhost:20001/pms/v1/api/swagger-ui/index.html  
- **📋 OpenAPI Spec**: http://localhost:20001/pms/v1/api/v3/api-docs
- **❤️ Health Check**: http://localhost:20001/pms/v1/api/actuator/health
- **🔐 Login Endpoint**: POST http://localhost:20001/pms/v1/api/auth/login

### Database Access
- **🗄️ PostgreSQL**: localhost:5432 (payroll_db / payroll_user / payroll_pass)
- **🗄️ PgAdmin Web UI**: http://localhost:5050
  - Email: `admin@payroll.com`
  - Password: `admin123`

### Default Security Credentials
- **Admin**: `admin` / `admin123` (Full system access)
- **Employees**: All use password `admin123`
  - `director001`, `manager001`, `senior001`, `senior002`
  - `dev001`, `dev002`, `junior001`, `junior002`
  - `intern001`, `intern002`

## 🔧 Manual Docker Commands (Advanced Users)

```bash
# Full system startup
docker-compose up --build -d

# View specific service logs  
docker-compose logs -f payroll-backend
docker-compose logs -f postgres
docker-compose logs -f redis

# Stop all services (keep data)
docker-compose down

# Complete cleanup (removes all data)
docker-compose down --volumes

# Rebuild only application (faster development)
docker-compose up --build payroll-backend
```

## 🧪 Quick API Testing

### 1. Health Check
```bash
curl http://localhost:20001/pms/v1/api/actuator/health
```

### 2. Admin Login & JWT Token
```bash
# Get JWT token
TOKEN=$(curl -s -X POST http://localhost:20001/pms/v1/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

echo "JWT Token: $TOKEN"
```

### 3. List All Employees (with Auth)
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:20001/pms/v1/api/employees | jq
```

### 4. Calculate Salary Sheet
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:20001/pms/v1/api/payroll/calculate | jq
```

### 5. Access Swagger UI
Open: http://localhost:20001/pms/v1/api/swagger-ui/index.html

## 🚨 Troubleshooting Guide

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| **Docker not running** | Start Docker Desktop first |
| **Port 20001 in use** | Kill process: `netstat -ano \| findstr :20001` then `taskkill /PID <pid> /F` |
| **Database connection timeout** | Wait 60-90 seconds for PostgreSQL to fully initialize |
| **Build failures** | Clean rebuild: `docker-compose down -v && docker-compose up --build` |
| **Liquibase migration errors** | Check database seed data and table creation order |
| **JWT token expired** | Re-login to get fresh token (24-hour expiration) |

### Log Analysis
```bash
# Check application startup logs
docker-compose logs payroll-backend | grep -i "started"

# Database connection issues  
docker-compose logs payroll-backend | grep -i "database"

# Authentication problems
docker-compose logs payroll-backend | grep -i "jwt\|auth"
```

### Development Tips
- Use **Swagger UI** for interactive API testing
- Check **PgAdmin** for database state verification  
- Monitor **Docker logs** for real-time debugging
- Use **Postman** for advanced API workflow testing