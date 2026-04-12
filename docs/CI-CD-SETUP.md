# CI/CD Setup & Configuration Guide

**Quick Reference for Docker, Jenkins, and GitHub Actions**

---

## 🚀 Getting Started (5 Minutes)

### 1. Configure Environment
```bash
# Copy configuration template
cp .env.example .env

# Verify configuration (optional - edit .env if needed)
docker-compose config
```

### 2. Start Services
```bash
# Stop old services (if running)
docker-compose down

# Start with new configuration
docker-compose up -d

# Verify all running
docker-compose ps
```

### 3. Test Setup
```bash
# Check application
curl http://localhost:20001/pms/v1/api/actuator/health

# View logs
docker logs payroll-backend
```

---

## 📋 Configuration Variables

All configuration is managed via `.env` file. Copy `.env.example` and customize:

| Variable | Default | Purpose |
|----------|---------|---------|
| `POSTGRES_DB` | `payroll_db` | Database name |
| `POSTGRES_USER` | `payroll_user` | Database user |
| `POSTGRES_PASSWORD` | `payroll_pass` | Database password |
| `SPRING_PROFILES_ACTIVE` | `docker` | Active Spring profiles |
| `PAYROLL_SERVICE_PORT` | `20001` | Application port |
| `POSTGRES_PORT` | `5432` | Database port |
| `PGADMIN_PORT` | `5050` | PgAdmin port |

**Full list:** See `.env.example` with all 20+ configurable variables

---

## 🌳 Spring Profiles

Application uses four standardized profiles:

| Profile | Usage | When |
|---------|-------|------|
| **`docker`** | Local development | `docker-compose up` (default) |
| **`docker,debug`** | Local debugging | `docker-compose -f docker-compose.yml -f docker-compose.debug.yml up` |
| **`ci`** | CI/CD (GA & Jenkins) | Automatic in GitHub Actions & Jenkinsfile |
| **`test`** | Unit/integration tests | `./gradlew test` during development |

See `application-ci.yml` for CI profile configuration.

---

## 🐳 Docker Compose

### Standard Setup (Local Development)
```bash
docker-compose up -d
```

**Services:**
- **payroll-service** (localhost:20001) - Spring Boot application
- **postgres** (localhost:5432) - PostgreSQL 17
- **pgadmin** (localhost:5050) - Database UI (dev only)

### Debug Mode (Remote Debugging)
```bash
# Start with debug enabled
docker-compose -f docker-compose.yml -f docker-compose.debug.yml up -d

# Connect debugger to localhost:5005
```

### Useful Commands
```bash
# View logs
docker-compose logs -f payroll-backend

# Check service status
docker-compose ps

# Stop all services
docker-compose down

# Remove volumes (clear data)
docker-compose down -v

# Restart a service
docker-compose restart payroll-backend
```

---

## 🔨 Building & Testing

### Build Application
```bash
./gradlew clean build -x test --no-daemon --info
```

### Run Tests
```bash
# Unit tests
./gradlew test --no-daemon --info

# Code quality checks
./gradlew check --no-daemon

# All checks (with CI profile)
SPRING_PROFILES_ACTIVE=ci ./gradlew clean build --no-daemon
```

### Gradle Options (Configured)
```
-Dorg.gradle.parallel=true     # Parallel compilation
-Dorg.gradle.workers.max=4     # Max worker threads
--no-daemon                    # No daemon process (clean builds)
--info                         # Verbose output
```

---

## 🔧 Jenkins Local Setup

> 📖 **Complete Guide:** See [JENKINS.md](JENKINS.md) for step-by-step Jenkins installation, configuration, and troubleshooting.

### Quick Start
```bash
# Use the startup script (recommended)
scripts\start-jenkins.bat        # Windows
./scripts/start-jenkins.sh       # Linux/macOS

# Or manually start
cd jenkins
docker-compose --env-file ../.env up -d
```

**Access:** http://localhost:8080

**Get admin password:**
```bash
docker exec payroll-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Jenkins Configuration
1. Unlock Jenkins with admin password
2. Install suggested plugins (Pipeline, GitHub, Docker)
3. Create admin user
4. Add GitHub credentials (PAT)
5. Configure webhook in GitHub repo settings

**Webhook URL:** `http://{YOUR_IP}:8080/jenkins/github-webhook/`

### Jenkins Pipelines
- **Jenkinsfile** in root - Declarative pipeline
- **jenkins/shared-library/vars/*.groovy** - Reusable functions
- **jenkins/docker-compose.yml** - Jenkins + PostgreSQL + PgAdmin

---

## 🐙 GitHub Actions

### Workflows (`.github/workflows/`)

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| **ci.yml** | PR, push develop/main | Build & test |
| **security.yml** | Daily + PR | Security scanning |
| **docker-build.yml** | Push main/develop | Docker image build |
| **deploy.yml** | Manual trigger | Deploy to environment |
| **codeql-analysis.yml** | Weekly + PR | Code analysis |

### Key Features
- ✅ Gradle caching (GitHub Actions cache)
- ✅ Docker layer caching
- ✅ PostgreSQL service for testing
- ✅ Artifact archival (build, test reports)
- ✅ PR comments with build status

---

## 📊 Version Standards

### Standardized Versions
- **PostgreSQL:** 17-alpine
- **Docker Compose:** 3.9
- **Java:** 24 (JDK/JRE)
- **Spring Boot:** 3.5.6
- **Gradle:** 8.14.3

### Health Checks
All services configured with retries=5:
- Application: Checks `/pms/v1/api/actuator/health`
- Database: Checks `pg_isready`
- Jenkins: Checks `/jenkins/login`

---

## 🔐 JVM Configuration

### Production JVM Options
```bash
-XX:+UseContainerSupport           # Docker awareness
-XX:MaxRAMPercentage=75.0          # Use 75% of container RAM
-XX:+UseG1GC                       # G1 garbage collector
-XX:+ExitOnOutOfMemoryError        # Fail fast on OOM
-Djava.security.egd=file:/dev/./urandom  # Fast random
```

### Jenkins JVM Options
```bash
-Xmx2g -Xms1g                      # 2GB max, 1GB initial heap
```

---

## 🔄 Port Configuration

All ports configurable via `.env`:

| Service | Default | Variable |
|---------|---------|----------|
| Application | 20001 | `PAYROLL_SERVICE_PORT` |
| PostgreSQL | 5432 | `POSTGRES_PORT` |
| PgAdmin | 5050 | `PGADMIN_PORT` |
| Jenkins | 8080 | `JENKINS_PORT` |
| Jenkins Agent | 50000 | `JENKINS_AGENT_PORT` |

**Change ports by editing `.env` before starting services.**

---

## 📈 Common Tasks

### Check Database Connection
```bash
docker exec payroll-postgres psql -U payroll_user -d payroll_db -c "SELECT 1"
```

### View Application Logs
```bash
docker logs -f payroll-backend
```

### Access Jenkins
```
http://localhost:8080/jenkins/
```

### Access PgAdmin
```
http://localhost:5050/
Email: admin@payroll.com
Password: admin123
```

### Monitor Docker Resources
```bash
docker stats payroll-backend payroll-postgres
```

### Clean Build (All)
```bash
# Clear Docker cache
docker system prune -a

# Full rebuild
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

## 🐛 Troubleshooting

### Services Won't Start
```bash
# Check logs
docker-compose logs

# Verify configuration
docker-compose config

# Check port conflicts
lsof -i :20001 :5432 :8080

# Ensure .env file exists
cp .env.example .env
```

### Database Connection Failed
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Test connection
docker exec payroll-postgres pg_isready -U payroll_user

# Check database exists
docker exec payroll-postgres psql -U payroll_user -l | grep payroll_db
```

### Build Times Slow
```bash
# Check Gradle cache
docker volume ls | grep gradle_cache

# Remove and rebuild
docker volume rm payroll_gradle_cache
docker-compose up -d --build
```

### Spring Profile Not Applied
```bash
# Verify environment variable
docker exec payroll-backend env | grep SPRING_PROFILES_ACTIVE

# Check active profiles in logs
docker logs payroll-backend | grep "profiles are active"
```

---

## ✅ Validation

Verify setup is correct:

```bash
# 1. Docker Compose valid
docker-compose config

# 2. Services running
docker-compose ps

# 3. Application responding
curl http://localhost:20001/pms/v1/api/actuator/health

# 4. Database responsive
docker exec payroll-postgres pg_isready -U payroll_user

# 5. Logs clean
docker-compose logs | grep -i error
```

---

## 📚 Additional Files

- **`.env.example`** - Configuration template (all variables with defaults)
- **`application-ci.yml`** - Spring CI/CD profile configuration
- **`Jenkinsfile`** - Jenkins pipeline definition (9 stages)
- **`.github/workflows/`** - GitHub Actions workflows (5 files)
- **`jenkins/docker-compose.yml`** - Jenkins local setup
- **`jenkins/shared-library/`** - Reusable Groovy functions

---

## 🔄 Refactoring Changes

All configuration is now:
- ✅ Standardized (PostgreSQL 17, Compose 3.9)
- ✅ Centralized (.env for all config)
- ✅ Consistent (same profiles, health checks, JVM opts)
- ✅ Documented (this guide + code comments)
- ✅ Zero duplication (configs not repeated)
- ✅ 100% backward compatible

---

**Last Updated:** April 12, 2026  
**Status:** Production Ready

