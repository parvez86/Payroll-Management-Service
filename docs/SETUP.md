# Setup & Quick Start Guide

## ⚡ Quick Start (2 Minutes)

### Windows
```bash
scripts\setup.bat
```

### Linux/Mac
```bash
chmod +x scripts/setup.sh
./scripts/setup.sh
```

**What happens:** 
- Creates `.env` from `.env.example` (if needed)
- Starts all services with Docker Compose
- Displays application URLs

After ~30 seconds, your application is ready!

---

## 🔧 Manual Setup (If Scripts Don't Work)

### 1. Create Environment File
```bash
# Copy the template
cp .env.example .env

# Verify it was created
cat .env
```

### 2. Start Services
```bash
docker-compose up -d
```

### 3. Verify Setup
```bash
# Check all services running
docker-compose ps

# Test application
curl http://localhost:20001/pms/v1/api/actuator/health
```

---

## 🌐 Access Your Application

| Service | URL | Credentials |
|---------|-----|-------------|
| **API** | http://localhost:20001/pms/v1/api | - |
| **Swagger UI** | http://localhost:20001/pms/v1/api/swagger-ui/index.html | - |
| **Health Check** | http://localhost:20001/pms/v1/api/actuator/health | - |
| **PgAdmin** | http://localhost:5050 | admin@payroll.com / admin123 |
| **Database** | localhost:5432 | payroll_user / payroll_pass |

---

## 📝 Configuration

### What is `.env`?

The `.env` file contains all environment variables. Created automatically from `.env.example` by the setup script.

### Customizing Configuration

Edit `.env` if you need different:
- **Port numbers:** `PAYROLL_SERVICE_PORT`, `POSTGRES_PORT`, `PGADMIN_PORT`
- **Database credentials:** `POSTGRES_USER`, `POSTGRES_PASSWORD`
- **Spring profile:** `SPRING_PROFILES_ACTIVE` (docker, debug, ci, test)
- **Logging level:** `LOGGING_LEVEL_ORG_SP_PAYROLL_SERVICE`

Then restart:
```bash
docker-compose down
docker-compose up -d
```

---

## 🐛 Troubleshooting

### Services Won't Start

**Check .env exists:**
```bash
ls -la .env          # Mac/Linux
dir .env             # Windows
```

**If missing:**
```bash
cp .env.example .env
```

### Docker Not Running
```bash
# Windows: Open Docker Desktop
# Mac: Open Applications > Docker
# Linux: sudo systemctl start docker
```

### Database Connection Failed
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Test connection
docker exec payroll-postgres psql -U payroll_user -d payroll_db -c "SELECT 1"
```

### Port Already in Use
Edit `.env` to change ports, then restart:
```bash
# Example: change app port from 20001 to 20002
PAYROLL_SERVICE_PORT=20002

# Restart
docker-compose down && docker-compose up -d
```

### View Logs
```bash
# Application logs
docker logs -f payroll-backend

# Database logs
docker logs -f payroll-postgres

# All services
docker-compose logs -f
```

---

## 📦 What Gets Installed

- **PostgreSQL 17** - Database (localhost:5432)
- **PgAdmin** - Database UI (localhost:5050)
- **Spring Boot 3.5.6** - Application (localhost:20001)
- **Java 24** - Runtime
- **Gradle 8.14.3** - Build tool

---

## 🛑 Stop Services

```bash
# Stop but keep data
docker-compose down

# Stop and remove everything (careful!)
docker-compose down -v
```

---

## 🔄 Restart Services

```bash
# Quick restart
docker-compose restart

# Full rebuild (if changes made to code)
docker-compose up -d --build
```

---

## 🚀 Building & Testing

### Build Application
```bash
./gradlew clean build -x test --no-daemon
```

### Run Tests
```bash
./gradlew test --no-daemon
```

### With CI Profile
```bash
SPRING_PROFILES_ACTIVE=ci ./gradlew clean build --no-daemon
```

---

## 🐛 Debug Mode

### Enable Remote Debugging
```bash
# Start with debug enabled
docker-compose -f docker-compose.yml -f docker-compose.debug.yml up -d

# Connect debugger to localhost:5005
```

---

## 📚 Additional Documentation

- **Jenkins Guide:** `docs/JENKINS.md` - Complete Jenkins installation, configuration & setup
- **CI/CD Guide:** `docs/CI-CD-SETUP.md` - GitHub Actions, Jenkins integration, Docker
- **API Documentation:** `docs/API_REVIEW_ANALYSIS.md` - All endpoints & requirements
- **Other Docs:** `docs/` folder - Architecture, debugging, security, credentials

---

## ✨ That's It!

Your Payroll Management System is now running locally. 

**Next steps:**
1. Open http://localhost:20001/pms/v1/api/swagger-ui/index.html
2. Explore the API endpoints
3. Read `docs/API_REVIEW_ANALYSIS.md` for API details
4. Check `docs/CI-CD-SETUP.md` for deployment & CI/CD info

**Questions?** Check the docs/ folder or view logs with `docker logs payroll-backend`
