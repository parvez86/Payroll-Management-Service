# Jenkins Configuration & Setup Guide

A comprehensive, step-by-step guide to install, configure, and run Jenkins for CI/CD pipelines. This guide is applicable to any project using Docker Compose.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Environment Setup](#environment-setup)
4. [Initial Authentication](#initial-authentication)
5. [Post-Configuration](#post-configuration)
6. [Pipeline Configuration](#pipeline-configuration)
7. [Verification & Testing](#verification--testing)
8. [Troubleshooting](#troubleshooting)
9. [Common Commands](#common-commands)

---

## Prerequisites

Before starting, ensure you have:

- **Docker Desktop** - Installed and running
- **Docker Compose** - Version 3.9 or higher
- **Port Availability** - Ports 8080 (Jenkins Web), 50000 (Jenkins Agent)
- **.env File** - Environment configuration (create from `.env.example` if missing)
- **Linux/macOS or Windows PowerShell** - For running commands
- **30 GB Free Disk Space** - For Jenkins home directory and caches

### Check Prerequisites

```bash
# Verify Docker is running
docker --version
docker ps

# Verify Docker Compose
docker-compose --version

# Check port availability
netstat -an | findstr :8080    # Windows
lsof -i :8080                  # macOS/Linux
```

---

## Installation

### Step 1: Verify Project Structure

Ensure your project has the following:

```
project-root/
├── .env.example              # Environment template
├── .env                       # Environment config (created from template)
├── docker-compose.yml         # Main services
├── jenkins/
│   └── docker-compose.yml     # Jenkins-specific services
├── scripts/
│   ├── start-jenkins.bat      # Windows startup script
│   └── start-jenkins.sh       # Linux/macOS startup script
└── docs/
    └── JENKINS.md             # This file
```

### Step 2: Create Environment Configuration

If `.env` doesn't exist, create it from the template:

```bash
# Windows (PowerShell)
Copy-Item .env.example .env

# Linux/macOS
cp .env.example .env
```

**Edit `.env` and configure:**

```env
# ==========================================
# JENKINS CONFIGURATION
# ==========================================
JENKINS_PORT=8080                          # Web UI port
JENKINS_AGENT_PORT=50000                   # Agent communication port
JENKINS_JAVA_OPTS=-Xmx2g -Xms1g           # Memory settings
JENKINS_OPTS=--prefix=/jenkins             # URL prefix (optional)

# Database for Jenkins testing
POSTGRES_DB=jenkins_db
POSTGRES_USER=jenkins_user
POSTGRES_PASSWORD=jenkins_pass

# PgAdmin for database management
PGADMIN_PORT=5050
PGADMIN_DEFAULT_EMAIL=admin@example.com
PGADMIN_DEFAULT_PASSWORD=admin123
```

### Step 3: Start Jenkins Services

**Option A: Using Startup Script (Recommended)**

```bash
# Windows
scripts\start-jenkins.bat

# Linux/macOS
chmod +x scripts/start-jenkins.sh
./scripts/start-jenkins.sh
```

**Option B: Manual Docker Compose**

```bash
cd jenkins
docker-compose --env-file ../.env up -d
```

### Step 4: Verify Services Started

```bash
# Check service status
docker-compose -f jenkins/docker-compose.yml --env-file .env ps

# Expected output:
# SERVICE    STATUS
# jenkins    Up X seconds (health: starting)
# postgres   Up X seconds (healthy)
# pgadmin    Up X seconds
```

### Step 5: Wait for Jenkins Initialization

Jenkins takes 60-120 seconds to fully initialize on first startup:

```bash
# Watch initialization progress
docker-compose -f jenkins/docker-compose.yml --env-file .env logs jenkins -f

# Wait for this message:
# Jenkins is fully up and running
```

---

## Initial Authentication

### Step 1: Retrieve Initial Admin Password

Jenkins auto-generates a temporary admin password on first startup:

```bash
# Get the password from container secrets
docker exec payroll-jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Output example:
# 7613a84c99c645c0a88d982691d6fd3e
```

### Step 2: Access Jenkins Web Interface

1. Open browser: **http://localhost:8080** (or configured port)
2. You should see the "Unlock Jenkins" page
3. Paste the password from Step 1

### Step 3: Install Suggested Plugins

Jenkins will prompt to install plugins:

```
✓ Select "Install suggested plugins"
✓ Wait for installation (5-10 minutes)
```

**Suggested Plugins Include:**
- Pipeline
- GitHub
- GitLab
- Docker
- Email Extension
- Blue Ocean
- Credentials
- SSH Agent

### Step 4: Create First Admin User

After plugins install, create your admin account:

```
Username: admin
Full Name: Jenkins Administrator
Email: admin@example.com
Password: [Create strong password]
```

**Save these credentials securely!**

---

## Post-Configuration

### Step 1: Configure Jenkins Base URL

1. **Menu** → **Manage Jenkins** → **Configure System**
2. Find **Jenkins Location** section
3. Set **Jenkins URL** to: `http://localhost:8080/jenkins`
4. Click **Save**

### Step 2: Configure Java Location

1. **Menu** → **Manage Jenkins** → **Configure System**
2. Scroll to **Java**
3. Path should auto-detect or enter: `/usr/local/openjdk-21/bin/java`
4. Click **Save**

### Step 3: Create SSH Credentials (For Git Repos)

1. **Menu** → **Manage Jenkins** → **Credentials**
2. Click **System** → **Global credentials**
3. Click **Add Credentials**
4. Select **SSH Username with private key**
5. Enter your SSH private key for GitHub/GitLab
6. Click **Create**

### Step 4: Configure GitHub Integration (Optional)

1. **Menu** → **Manage Jenkins** → **Configure System**
2. Find **GitHub** section
3. Add GitHub API token:
   - Go to GitHub → Settings → Developer settings → Personal access tokens
   - Create token with `repo` and `repo:status` scopes
   - Paste in Jenkins GitHub configuration
4. Click **Test Connection**
5. Click **Save**

### Step 5: Enable API Token Authentication

To use Jenkins API with scripts:

1. Click your **username** (top-right corner)
2. Click **Configure**
3. Scroll to **API Token** section
4. Click **Add new Token**
5. Name it: `ci-automation`
6. Copy the token (you'll use this in CI scripts)
7. Click **Save**

---

## Pipeline Configuration

### Step 1: Create a New Pipeline Job

1. **Dashboard** → **New Item**
2. Enter job name: `payroll-ci-pipeline`
3. Select **Pipeline**
4. Click **OK**

### Step 2: Configure Pipeline from Git

1. Under **Advanced project options** → **Definition**
2. Select **Pipeline script from SCM**
3. SCM: **Git**
4. Repository URL: `https://github.com/yourusername/payroll-service.git`
5. Credentials: Select your SSH credentials (or create new)
6. Script Path: `Jenkinsfile`
7. Click **Save**

### Step 3: Create Jenkinsfile in Repository

Create `Jenkinsfile` in repository root:

```groovy
pipeline {
    agent any
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                script {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build'
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    sh './gradlew test'
                }
            }
        }
        
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'build/libs/*.jar', 
                                 allowEmptyArchive: true
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        failure {
            echo 'Pipeline failed!'
        }
        success {
            echo 'Pipeline succeeded!'
        }
    }
}
```

### Step 4: Test the Pipeline

1. Go to your job: **Dashboard** → **payroll-ci-pipeline**
2. Click **Build Now**
3. Watch build progress in **Console Output**

---

## Verification & Testing

### Test 1: Jenkins Web Interface

```
✅ Visit http://localhost:8080
✅ Login with your credentials
✅ See Dashboard with jobs listed
```

### Test 2: Service Health

```bash
# Check all services healthy
docker-compose -f jenkins/docker-compose.yml --env-file .env ps

# Expected: 
# - jenkins: healthy
# - postgres: healthy
# - pgadmin: up
```

### Test 3: Jenkins API

```bash
# Test Jenkins API (replace with your token)
curl -u admin:YOUR_API_TOKEN http://localhost:8080/api/json

# Should return JSON with Jenkins info
```

### Test 4: Database Connectivity

1. Open **http://localhost:5050** (PgAdmin)
2. Login: `admin@payroll.com` / `admin123`
3. Add PostgreSQL server:
   - Host: `payroll-jenkins-db` (internal Docker DNS)
   - Port: `5432`
   - Database: Value from `$POSTGRES_DB` in `.env`
   - Username: Value from `$POSTGRES_USER` in `.env`
   - Password: Value from `$POSTGRES_PASSWORD` in `.env`
4. Should connect successfully

### Test 5: Build Execution

1. Create a simple test job:
   - **New Item** → **Freestyle job** → name: `test-build`
   - **Build Steps** → **Execute Shell**
   - Add command: `echo "Jenkins working!"`
   - Click **Save**
2. Click **Build Now**
3. Check **Console Output** shows: `Jenkins working!`

---

## Troubleshooting

### Problem: "Cannot connect to Jenkins"

**Solution:**
```bash
# Check if container is running
docker ps | grep jenkins

# Check if port 8080 is in use
netstat -an | findstr :8080

# View Jenkins logs
docker-compose -f jenkins/docker-compose.yml logs jenkins --tail 50
```

### Problem: "Health check failing"

**Solution:**
```bash
# Jenkins takes time on first start (2-3 minutes)
# Wait longer and check again
sleep 120
docker-compose -f jenkins/docker-compose.yml ps

# If still failing, check logs
docker logs payroll-jenkins -f
```

### Problem: "Cannot find initialAdminPassword"

**Solution:**
```bash
# File doesn't exist if not initialized
# Restart Jenkins and wait for full initialization
docker-compose -f jenkins/docker-compose.yml restart jenkins
docker logs payroll-jenkins -f

# Wait for "Jenkins is fully up and running" message
```

### Problem: "Java heap space error"

**Solution:**
```bash
# Increase Java memory in .env
JENKINS_JAVA_OPTS=-Xmx4g -Xms2g

# Restart Jenkins
docker-compose -f jenkins/docker-compose.yml down
docker-compose -f jenkins/docker-compose.yml up -d
```

### Problem: "Plugins failing to install"

**Solution:**
```bash
# Restart plugin installation
docker exec payroll-jenkins touch /var/jenkins_home/jenkins.install.InstallUtil.lastExecVersion

# Restart Jenkins
docker-compose -f jenkins/docker-compose.yml restart jenkins
```

### Problem: "Cannot connect to GitHub"

**Solution:**
1. Verify SSH key is added to GitHub account
2. Test connection manually:
   ```bash
   docker exec payroll-jenkins ssh -T git@github.com
   # Should show: Hi username! You've successfully authenticated...
   ```
3. Check Jenkins GitHub credentials configuration
4. Verify repository URL is correct

---

## Common Commands

### Start/Stop Services

```bash
# Start Jenkins (using script)
scripts/start-jenkins.bat                           # Windows
./scripts/start-jenkins.sh                          # Linux/macOS

# Start manually
docker-compose -f jenkins/docker-compose.yml --env-file .env up -d

# Stop Jenkins
docker-compose -f jenkins/docker-compose.yml down

# Stop and remove volumes (clean start)
docker-compose -f jenkins/docker-compose.yml down -v
```

### View Logs

```bash
# Jenkins service logs
docker-compose -f jenkins/docker-compose.yml logs jenkins

# Follow logs in real-time
docker-compose -f jenkins/docker-compose.yml logs jenkins -f

# Last 50 lines
docker-compose -f jenkins/docker-compose.yml logs jenkins --tail 50

# Database logs
docker-compose -f jenkins/docker-compose.yml logs postgres
```

### Access Jenkins Console

```bash
# Execute bash in Jenkins container
docker exec -it payroll-jenkins bash

# View Jenkins configuration
docker exec payroll-jenkins cat /var/jenkins_home/config.xml | head -50
```

### Backup & Restore

```bash
# Backup Jenkins home directory
docker cp payroll-jenkins:/var/jenkins_home ./jenkins_backup_$(date +%Y%m%d)

# Restore from backup
docker cp ./jenkins_backup_20240412 payroll-jenkins:/var/jenkins_home
```

### Reset Jenkins

```bash
# Complete reset (deletes all data)
docker-compose -f jenkins/docker-compose.yml down -v

# Restart fresh
docker-compose -f jenkins/docker-compose.yml up -d

# Get new initial admin password
docker exec payroll-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

---

## Performance Tuning

### For Large Pipelines

```env
# .env configuration
JENKINS_JAVA_OPTS=-Xmx4g -Xms3g -XX:+UseG1GC -XX:MaxGCPauseMillis=30
JENKINS_OPTS=--prefix=/jenkins -Dhudson.threads.executors=4 -Dhudson.model.LoadStatistics.decay=0.9
```

### For High Concurrency

```bash
# Enable distributed builds with agents
# See Jenkins documentation for agent configuration
```

### Disable Unused Plugins

```bash
# Menu → Manage Jenkins → Manage Plugins → Installed
# Unmark plugins you don't use (keeps memory low)
```

---

## Security Best Practices

### 1. Change Default Credentials

```
✅ Create strong admin password
✅ Remove anonymous access
✅ Use API tokens instead of passwords
```

### 2. Configure Security Realm

1. **Manage Jenkins** → **Configure System**
2. Under **Security Realm**, select one:
   - **Jenkins' own user database** (simple)
   - **GitHub Authentication** (enterprise)
   - **LDAP** (corporate environments)

### 3. Set Up HTTPS

```bash
# Generate self-signed certificate
docker exec payroll-jenkins bash -c \
  'keytool -genkey -alias hudson -keyalg RSA -keystore $JENKINS_HOME/keystore.jks'

# Configure Jenkins to use HTTPS (in Jenkins UI)
```

### 4. Enable CSRF Protection

1. **Manage Jenkins** → **Configure System**
2. Check **CSRF Protection** ✓
3. Save

### 5. Restrict Job Creation

1. **Manage Jenkins** → **Configure Global Security**
2. Under **Authorization**, set appropriate permissions
3. Save

---

## Advanced Configuration

### Multi-Agent Setup (Distributed Builds)

```groovy
// Jenkinsfile example
pipeline {
    agent {
        label 'linux'  // Requires agent with 'linux' label
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'echo "Running on remote agent"'
            }
        }
    }
}
```

### Declarative Pipeline with Credentials

```groovy
pipeline {
    agent any
    
    environment {
        GITHUB_CREDS = credentials('github-ssh-key')
        DB_PASS = credentials('database-password')
    }
    
    stages {
        stage('Build') {
            steps {
                script {
                    sh 'echo "Building with credentials"'
                }
            }
        }
    }
}
```

### Webhook Integration (GitHub)

1. In GitHub: **Repository** → **Settings** → **Webhooks**
2. Add webhook:
   - Payload URL: `http://your-jenkins-server:8080/github-webhook/`
   - Content type: `application/json`
   - Which events: `Push events`
3. In Jenkins: Job → **Configure** → **Build Triggers**
4. Check: **GitHub hook trigger for GITScm polling**

---

## Maintenance

### Weekly Tasks

```bash
# Check disk space
docker exec payroll-jenkins df -h

# Check Jenkins logs for errors
docker logs payroll-jenkins | grep -i error

# Backup Jenkins configuration
docker cp payroll-jenkins:/var/jenkins_home/config.xml ./backup/
```

### Monthly Tasks

```bash
# Update Jenkins image
docker pull jenkins/jenkins:lts-jdk21

# Restart with new image
docker-compose -f jenkins/docker-compose.yml down
docker-compose -f jenkins/docker-compose.yml up -d

# Update plugins
# Manage Jenkins → Manage Plugins → Check for Updates
```

### Quarterly Tasks

```bash
# Review and clean old builds
# Manage Jenkins → Configure System → Log Rotation

# Archive and delete old job data
docker exec payroll-jenkins bash -c \
  'find /var/jenkins_home/jobs -name "builds" -type d -mtime +90 -exec rm -rf {} \;'
```

---

## Summary Checklist

- [ ] Docker & Docker Compose installed
- [ ] .env file created from .env.example
- [ ] Jenkins services started successfully
- [ ] Initial admin password retrieved
- [ ] Logged in to Jenkins Web UI
- [ ] Suggested plugins installed
- [ ] Admin account created
- [ ] Base URL configured
- [ ] SSH credentials added
- [ ] Database connectivity verified
- [ ] Test pipeline executed successfully
- [ ] Backups scheduled

---

## References

- [Jenkins Official Documentation](https://www.jenkins.io/doc/)
- [Jenkins Docker Image](https://github.com/jenkinsci/docker)
- [Jenkins Pipeline Guide](https://www.jenkins.io/doc/book/pipeline/)
- [Jenkins Security Best Practices](https://www.jenkins.io/doc/book/security/)

---

**Document Version:** 1.0  
**Last Updated:** April 2026  
**Applicable To:** Jenkins LTS with Docker Compose  
**Audience:** DevOps Engineers, CI/CD Teams, System Administrators
