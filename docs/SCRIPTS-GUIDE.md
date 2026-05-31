# Payroll Management System - Scripts Guide

## 📋 Overview

This guide maps all startup and setup scripts to their corresponding Spring profiles, environment configurations, and use cases. Choose the right script for your scenario.

---

## 🎯 Script Selection Matrix

| Script | Profile(s) | Use Case | Environment | Database |
|--------|-----------|----------|-------------|----------|
| `start-payroll.bat` | `docker` | Production-like environment | Docker Container | PostgreSQL |
| `start-debug.bat` | `docker,debug` | Remote debugging & development | Docker Container | PostgreSQL |
| `dev-setup.bat` | `docker` + pgAdmin | Full dev environment with UI tools | Docker Container | PostgreSQL |
| `start-jenkins.bat` | N/A (Jenkins only) | CI/CD pipeline setup | Docker Container | Separate |
| `setup-cicd.bat` | `dev` (local) | Local CI/CD development | Local Machine | PostgreSQL |
| `setup.bat` | `docker` | Initial project setup | Docker Container | PostgreSQL |

---

## 🚀 **SCRIPT 1: start-payroll.bat** (Production-Like)

### Profile Configuration
```yaml
SPRING_PROFILES_ACTIVE: docker
Profile File: application-docker.yml
```

### What It Does
- ✅ Starts Payroll Service in Docker
- ✅ Starts PostgreSQL database
- ✅ Starts pgAdmin (for development)
- ❌ NO debug mode
- ❌ NO Jenkins
- ✅ Production-ready logging

### When to Use
- Running in staging environment
- Testing with production configuration
- Integration testing
- Performance testing

### Command
```cmd
scripts\start-payroll.bat
```

### URLs After Startup
```
🌐 API:            http://localhost:20001/pms/v1/api
📚 Swagger:        http://localhost:20001/pms/v1/api/swagger-ui/index.html
❤️ Health:         http://localhost:20001/pms/v1/api/actuator/health
🗄️ PgAdmin:       http://localhost:5050 (admin@payroll.com / admin123)
```

### Configuration Source
```
.env (reads SPRING_PROFILES_ACTIVE=docker)
```

---

## 🔧 **SCRIPT 2: start-debug.bat** (Debugging & Development)

### Profile Configuration
```yaml
SPRING_PROFILES_ACTIVE: docker,debug
Profile Files: 
  - application-docker.yml (primary)
  - application-debug.yml (overrides)
```

### What It Does
- ✅ Starts Payroll Service in Docker with remote debugging enabled
- ✅ Exposes debug port: 5005
- ✅ Enables detailed SQL logging with formatted output
- ✅ Enables Hibernate statistics
- ✅ Enables all Actuator endpoints
- ✅ Sets batch_size: 1 (no batching for debugging clarity)
- ✅ Starts PostgreSQL database
- ✅ Starts pgAdmin

### When to Use
- Debugging Java code with IDE
- Analyzing SQL queries in detail
- Understanding transaction flow
- Development with real database
- Monitoring Hibernate behavior

### Command
```cmd
scripts\start-debug.bat
```

### Remote Debug Connection
```
IDE Configuration:
  - Host: localhost
  - Port: 5005
  - Transport: Socket
  
Steps:
  1. In IntelliJ: Run → Edit Configurations → Remote JVM Debug
  2. Set Host=localhost, Port=5005
  3. Click "Debug" to start debugging
```

### Debug Features Enabled
- ✅ SQL logging with formatting: `format_sql: true`
- ✅ Transaction debugging: `show_sql: true`
- ✅ Security debug logging: DEBUG level
- ✅ Hibernate statistics: `generate_statistics: true`
- ✅ Spring Web debugging: DEBUG level
- ✅ Spring Data debugging: DEBUG level
- ✅ All Actuator endpoints exposed

### Logging Configuration
```yaml
org.sp.payroll_service: DEBUG
org.springframework.security: DEBUG
org.springframework.web: DEBUG
org.springframework.data: DEBUG
org.hibernate.SQL: DEBUG
org.hibernate.type.descriptor.sql.BasicBinder: TRACE
liquibase: DEBUG
```

### URLs After Startup
```
🌐 API:            http://localhost:20001/pms/v1/api
📚 Swagger:        http://localhost:20001/pms/v1/api/swagger-ui/index.html
❤️ Health:         http://localhost:20001/pms/v1/api/actuator/health
🔧 Debug Port:     5005 (for IDE attachment)
🗄️ PgAdmin:       http://localhost:5050 (admin@payroll.com / admin123)
📊 Actuator:       http://localhost:20001/pms/v1/api/actuator/
```

### Configuration Source
```
docker-compose.debug.yml (composite profile)
```

---

## 🛠️ **SCRIPT 3: dev-setup.bat** (Development Environment Setup)

### Profile Configuration
```yaml
SPRING_PROFILES_ACTIVE: docker
Docker Profile: dev
Profile File: application-docker.yml
```

### What It Does
- ✅ Pulls latest Docker images
- ✅ Builds Docker containers
- ✅ Starts Payroll Service in Docker
- ✅ Starts PostgreSQL database
- ✅ Starts pgAdmin (WITH profile: dev)
- ✅ Verifies service health
- ✅ Displays useful URLs

### When to Use
- First-time development setup
- Refreshing Docker environment
- Setting up development machine
- Team onboarding

### Command
```cmd
scripts\dev-setup.bat
```

### Services Started
```
✅ payroll-service    (Spring Boot application)
✅ postgres           (PostgreSQL database)
✅ pgadmin            (PgAdmin UI - dev profile)
```

### Docker Compose Profile
```
--profile dev
This activates: pgAdmin service only in dev environment
```

### URLs After Startup
```
🌐 API:            http://localhost:20001/pms/v1/api
📚 Swagger:        http://localhost:20001/pms/v1/api/swagger-ui/index.html
❤️ Health:         http://localhost:20001/pms/v1/api/actuator/health
🗄️ PgAdmin:       http://localhost:5050 (admin@payroll.com / admin123)
📊 PostgreSQL:     localhost:5432 (payroll_user / payroll_pass)
```

### Configuration Source
```
.env (reads SPRING_PROFILES_ACTIVE=docker)
docker-compose.yml --profile dev
```

---

## 🏗️ **SCRIPT 4: start-jenkins.bat** (CI/CD Pipeline)

### Profile Configuration
```yaml
Jenkins Profile: N/A (Jenkins-specific, not Spring Boot)
Configuration: jenkins/docker-compose.yml (separate)
```

### What It Does
- ✅ Starts Jenkins CI/CD server
- ✅ Starts Jenkins Agent
- ✅ Configures shared library
- ❌ Does NOT start Payroll Service
- ❌ Does NOT start database
- ✅ Jenkins runs on port 8080

### When to Use
- Setting up CI/CD pipelines
- Running automated builds
- Configuring GitHub webhooks
- Setting up branch protection
- Implementing multi-branch pipelines

### Command
```cmd
scripts\start-jenkins.bat
```

### Jenkins Features
```
✅ Blue Ocean UI (modern pipeline visualization)
✅ GitHub webhook triggers (instant builds)
✅ Email notifications
✅ GitHub status checks
✅ Branch protection enforcement
```

### URLs After Startup
```
🔧 Jenkins:        http://localhost:8080
📚 Jenkins Docs:   http://localhost:8080/jenkins
🔌 GitHub Webhooks configured at: /github-webhook/
```

### Configuration
```
jenkins/docker-compose.yml
Environment variables from .env file
```

### Important Note
```
⚠️ Jenkins and Payroll Service are INDEPENDENT
   - Use start-jenkins.bat for Jenkins
   - Use start-payroll.bat for Payroll Service
   - Run both simultaneously if building Jenkins pipeline for Payroll Service
```

---

## 💻 **SCRIPT 5: setup-cicd.bat** (Local CI/CD Development)

### Profile Configuration
```yaml
SPRING_PROFILES_ACTIVE: dev (LOCAL, not Docker)
Profile File: application-dev.yml
Environment: Local Machine (not containerized)
```

### What It Does
- ✅ Creates .env.local with dev profile
- ✅ Sets up local CI/CD hooks
- ✅ Configures pre-commit hooks
- ✅ Sets up local environment variables
- ❌ Does NOT start Docker
- ❌ Does NOT start database
- ✅ Prepares local development environment

### When to Use
- Local development (NOT Docker)
- Testing CI/CD configuration locally
- Setting up pre-commit hooks
- Developer machine setup
- Testing with H2 in-memory database

### Command
```cmd
scripts\setup-cicd.bat
```

### Generated Configuration
```
.env.local file is created with:

SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payroll_db
SPRING_JPA_SHOW_SQL=false
LOGGING_LEVEL_ORG_SP_PAYROLL=DEBUG
```

### Database Configuration
```
🗄️ PostgreSQL: localhost:5432 (if running locally)
   OR
🗄️ H2 In-Memory: mem:payroll_dev (dev profile uses H2)
```

### Application Settings (Local Dev)
```yaml
Port: 20001
Context Path: /pms
JPA Hibernate: validate (Liquibase handles schema)
H2 Console: /h2-console (if using H2)
```

### Important Notes
```
⚠️ This script does NOT start Docker
⚠️ Requires local database (PostgreSQL) or H2 setup
⚠️ Use for local development, not production
⚠️ H2 is in-memory, data lost on restart
```

---

## ⚙️ **SCRIPT 6: setup.bat** (Initial Setup)

### Profile Configuration
```yaml
SPRING_PROFILES_ACTIVE: docker
Profile File: application-docker.yml
```

### What It Does
- ✅ Creates .env from .env.example (if not exists)
- ✅ Verifies Docker is running
- ✅ Builds Docker containers
- ✅ Starts Payroll Service
- ✅ Verifies service health
- ✅ Displays URLs

### When to Use
- First-time project setup
- Fresh installation
- New environment setup
- Docker initialization

### Command
```cmd
scripts\setup.bat
```

### Configuration Source
```
.env created from .env.example
SPRING_PROFILES_ACTIVE=docker (default)
```

---

## 📊 Profile Comparison Table

| Feature | docker | docker,debug | dev | ci |
|---------|--------|-------------|-----|-----|
| **Database** | PostgreSQL | PostgreSQL | H2 or PostgreSQL | PostgreSQL |
| **Environment** | Docker Container | Docker Container | Local Machine | CI/CD Server |
| **SQL Logging** | WARN | DEBUG | DEBUG | WARN |
| **Show SQL** | false | true | true | false |
| **Format SQL** | false | true | true | false |
| **Batch Size** | 20 | 1 | 20 | 20 |
| **Generate Stats** | true | true | false | false |
| **Debug Port** | None | 5005 | None | None |
| **Actuator** | Limited | All | All | Limited |
| **Security Log** | WARN | DEBUG | DEBUG | WARN |
| **Use Case** | Production | Development | Local Dev | CI/CD Builds |

---

## 🔄 Environment Variables Mapping

### .env (Docker Containers)
```env
SPRING_PROFILES_ACTIVE=docker              # or: docker,debug for debug mode
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/payroll_db
SPRING_DATASOURCE_USERNAME=payroll_user
SPRING_DATASOURCE_PASSWORD=payroll_pass
JWT_SECRET=DockerSecretKeyForPayrollSystemProduction2024
```

### .env.local (Local Development)
```env
SPRING_PROFILES_ACTIVE=dev                 # local development with H2
SPRING_DATASOURCE_URL=jdbc:h2:mem:payroll_dev
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=
JWT_SECRET=DevSecretKeyForPayrollSystemTesting2024
```

### CI/CD Environment Variables
```env
SPRING_PROFILES_ACTIVE=ci                  # CI-specific configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/payroll_db
SPRING_DATASOURCE_USERNAME=payroll_user
SPRING_DATASOURCE_PASSWORD=payroll_pass
JWT_SECRET=CISecretKeyForPayrollSystemTesting2024
```

---

## 🎓 Usage Scenarios

### Scenario 1: "I want to develop with real database"
```bash
# Use start-payroll.bat
scripts\start-payroll.bat

# Profile: docker
# Database: PostgreSQL in Docker
# Debug: No
```

### Scenario 2: "I want to debug code in my IDE"
```bash
# Use start-debug.bat
scripts\start-debug.bat

# Profile: docker,debug
# Database: PostgreSQL in Docker
# Debug: Yes (port 5005)
# Connect IDE debugger to localhost:5005
```

### Scenario 3: "I'm setting up development environment for first time"
```bash
# Use dev-setup.bat
scripts\dev-setup.bat

# Profile: docker
# Database: PostgreSQL in Docker
# Includes: pgAdmin for database UI
# This is comprehensive setup
```

### Scenario 4: "I want to setup Jenkins CI/CD"
```bash
# Use start-jenkins.bat (in parallel with start-payroll.bat)
scripts\start-jenkins.bat

# Jenkins runs on port 8080
# Configure GitHub webhooks after startup
# See docs/START-HERE-5-FEATURES.md
```

### Scenario 5: "I'm developing locally without Docker"
```bash
# Use setup-cicd.bat
scripts\setup-cicd.bat

# Profile: dev
# Database: H2 in-memory (or local PostgreSQL)
# No Docker containers
# Faster local development
```

### Scenario 6: "First time setup, default configuration"
```bash
# Use setup.bat
scripts\setup.bat

# Profile: docker
# Sets up everything from scratch
# Creates .env file
# Verifies Docker
# Starts services
```

---

## 🐳 Docker Compose Profiles

### Profile: dev
```bash
# Includes: pgAdmin for database UI
docker-compose --profile dev up

# Services started:
#   - postgres
#   - payroll-service
#   - pgAdmin (database UI)
```

### Profile: default (no profile specified)
```bash
# Includes: Only essential services (NO pgAdmin)
docker-compose up

# Services started:
#   - postgres
#   - payroll-service
```

### Profile: debug
```bash
# Used by start-debug.bat
# Applies: application-debug.yml overrides
# Services started:
#   - postgres
#   - payroll-service (with debug settings)
#   - pgAdmin
```

---

## ✅ Pre-Requisites Before Running Scripts

### All Scripts Require:
```
✅ Docker Desktop installed and running
✅ .env file (created automatically or copy from .env.example)
✅ Git repository (for CI/CD setup scripts)
✅ Windows (for .bat files) or Linux/Mac (for .sh files)
```

### For start-debug.bat Additional:
```
✅ IDE with remote debugger support (IntelliJ, Eclipse, VS Code)
✅ Port 5005 available (debug port)
```

### For setup-cicd.bat Additional:
```
✅ Local PostgreSQL running (or modify to use H2)
✅ Git repository initialized
```

---

## 🚨 Common Issues & Solutions

### Issue: "Docker is not running"
```bash
# Solution: Start Docker Desktop first
# Windows: Click Docker Desktop icon in taskbar
# Linux: sudo systemctl start docker
# Mac: open /Applications/Docker.app
```

### Issue: "Port 5005 already in use (debug)"
```bash
# Solution: Change debug port in docker-compose.debug.yml
debug:
  ports:
    - "5005:5005"  # Change first number to 5006, 5007, etc
```

### Issue: "Liquibase checksum error"
```bash
# Solution: Ensure clean database
docker-compose down
docker volume rm payroll_postgres_data
docker-compose up --build
```

### Issue: "Cannot connect to database"
```bash
# Solution: Verify PostgreSQL is running
docker-compose ps

# Should show postgres service as "Up"
# If not: docker-compose up postgres -d
```

---

## 📚 Related Documentation

For more detailed information, see:
- **Debugging:** `docs/DEBUG_GUIDE.md`
- **Setup:** `docs/BACKEND-COMPLETE-SETUP-STEPS.md`
- **CI/CD:** `docs/CI-CD-SETUP.md`
- **Jenkins 5 Features:** `docs/START-HERE-5-FEATURES.md`
- **Credentials:** `docs/TEST-CREDENTIALS.md`

---

## 🎯 Quick Decision Tree

```
START HERE: What do you want to do?

1. Run application with database?
   → YES: start-payroll.bat ✅
   
2. Debug Java code with IDE?
   → YES: start-debug.bat ✅
   
3. First-time setup with all tools?
   → YES: dev-setup.bat ✅
   
4. Setup Jenkins CI/CD?
   → YES: start-jenkins.bat ✅
   
5. Develop locally without Docker?
   → YES: setup-cicd.bat ✅
   
6. Fresh project initialization?
   → YES: setup.bat ✅
```

---

## 📝 Summary

| Goal | Script | Profile | Database | Environment |
|------|--------|---------|----------|-------------|
| Production-like | start-payroll.bat | docker | PostgreSQL | Docker |
| Remote debug | start-debug.bat | docker,debug | PostgreSQL | Docker |
| Full dev setup | dev-setup.bat | docker | PostgreSQL | Docker |
| Jenkins CI/CD | start-jenkins.bat | N/A | Separate | Docker |
| Local dev | setup-cicd.bat | dev | H2/Local | Local |
| First setup | setup.bat | docker | PostgreSQL | Docker |

---

**Last Updated:** May 31, 2026  
**Version:** 1.0  
**Status:** ✅ Complete & Accurate
