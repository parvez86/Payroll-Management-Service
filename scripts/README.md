# Quick Start Scripts

## 🚀 First Time Setup

```bash
scripts\setup.bat              # Windows - Initial setup (creates .env, starts Payroll Service)
./scripts/setup.sh            # Linux/Mac
```

## 🚀 Payroll Service (Main Application)

```bash
scripts\start-payroll.bat      # Windows - Start the application
./scripts/start-payroll.sh    # Linux/Mac
```

## 🚀 Jenkins CI/CD (Optional)

```bash
scripts\start-jenkins.bat      # Windows - Start Jenkins on port 8080
./scripts/start-jenkins.sh    # Linux/Mac
```

### 🔑 Jenkins Setup with 5 New Features

After starting Jenkins, complete this setup for production-grade CI/CD:

```bash
# 📖 See complete setup guide
docs/START-HERE-5-FEATURES.md         # 5-minute quick guide
docs/SETUP-5-FEATURES-CHECKLIST.md    # Step-by-step implementation (45-60 min)
```

**5 Features Implemented:**
1. ✅ **GitHub Webhook Triggers** - Instant builds (10 sec vs 60 min)
2. ✅ **Email Notifications** - Auto alerts on success/failure
3. ✅ **Blue Ocean UI** - Modern visual pipeline interface
4. ✅ **GitHub Status Checks** - 3 checks on PR (Build, Tests, Security)
5. ✅ **Branch Protection** - Enforced quality gates on master/develop

## What These Scripts Do

- ✅ Create `.env` from `.env.example` (if needed)
- ✅ Verify Docker is running
- ✅ Start Docker Compose services
- ✅ Display service URLs
- ✅ Show logs for debugging

## 📖 Full Documentation

- **CI/CD Setup:** See `docs/CI-CD-SETUP.md`
- **Jenkins 5-Features:** See `docs/INDEX-5-FEATURES.md` (complete index)
- **Quick Start:** See `docs/START-HERE-5-FEATURES.md`

## 📋 Application URLs

- **API:** http://localhost:20001/pms/v1/api
- **Swagger:** http://localhost:20001/pms/v1/api/swagger-ui/index.html
- **Health:** http://localhost:20001/pms/v1/api/actuator/health
- **PgAdmin:** http://localhost:5050 (admin@payroll.com / admin123)

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