#!/usr/bin/env bash
# Setup CI/CD configuration for local development
# This script helps developers set up local pre-commit hooks and configuration

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   Payroll CI/CD Local Development Setup${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
echo ""

# Check if we're in a git repository
if [ ! -d ".git" ]; then
    echo -e "${RED}❌ Not a git repository. Run this script from the project root.${NC}"
    exit 1
fi

# 1. Install pre-commit hook
echo -e "${YELLOW}📋 Setting up pre-commit hooks...${NC}"

HOOK_DIR=".git/hooks"
HOOK_FILE="$HOOK_DIR/pre-commit"

if [ ! -d "$HOOK_DIR" ]; then
    mkdir -p "$HOOK_DIR"
fi

if [ -f "scripts/pre-commit" ]; then
    cp scripts/pre-commit "$HOOK_FILE"
    chmod +x "$HOOK_FILE"
    echo -e "${GREEN}✅ Pre-commit hook installed${NC}"
else
    echo -e "${YELLOW}⚠️  Pre-commit script not found at scripts/pre-commit${NC}"
fi

# 2. Create environment file template
echo ""
echo -e "${YELLOW}📝 Creating environment configuration...${NC}"

if [ ! -f ".env.local" ]; then
    cat > ".env.local" <<'EOF'
# Local Development Environment Variables
# Copy this file to .env.local and update values as needed

# Spring Profile
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payroll_db
SPRING_DATASOURCE_USERNAME=payroll_user
SPRING_DATASOURCE_PASSWORD=payroll_pass

# JWT Configuration
JWT_SECRET=your-secret-key-change-in-production-at-least-256-bits
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=20001
SERVER_SERVLET_CONTEXT_PATH=/pms

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=INFO
LOGGING_LEVEL_ORG_SP_PAYROLL=DEBUG

# Actuator
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics,info

# Docker
DOCKER_HOST=unix:///var/run/docker.sock
EOF
    echo -e "${GREEN}✅ Created .env.local template${NC}"
    echo -e "${YELLOW}   Update .env.local with your local configuration${NC}"
else
    echo -e "${BLUE}ℹ️  .env.local already exists${NC}"
fi

# 3. Create gradle.properties if needed
echo ""
echo -e "${YELLOW}🔧 Configuring Gradle...${NC}"

if [ ! -f "gradle.properties" ]; then
    cat >> gradle.properties <<'EOF'

# CI/CD Configuration
org.gradle.daemon=false
org.gradle.parallel=true
org.gradle.workers.max=4

# Build Configuration
build.info.enabled=true
EOF
    echo -e "${GREEN}✅ Gradle configuration updated${NC}"
else
    echo -e "${BLUE}ℹ️  gradle.properties already exists${NC}"
fi

# 4. Setup Spotless (if not already configured)
echo ""
echo -e "${YELLOW}🎨 Checking code formatting configuration...${NC}"

if ! grep -q "spotless" build.gradle 2>/dev/null; then
    echo -e "${YELLOW}⚠️  Spotless not configured. Consider adding it to build.gradle${NC}"
    echo "   Documentation: https://github.com/diffplug/spotless"
else
    echo -e "${GREEN}✅ Spotless is configured${NC}"
fi

# 5. Verify Gradle wrapper
echo ""
echo -e "${YELLOW}✅ Verifying Gradle wrapper...${NC}"

if [ ! -f "gradlew" ]; then
    echo -e "${YELLOW}⚠️  Gradle wrapper not found${NC}"
else
    chmod +x gradlew
    echo -e "${GREEN}✅ Gradle wrapper ready${NC}"
fi

# 6. Build project test
echo ""
echo -e "${YELLOW}🏗️  Testing build configuration...${NC}"

if ./gradlew clean --no-daemon > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Gradle build system is ready${NC}"
else
    echo -e "${RED}❌ Issues with Gradle configuration${NC}"
    echo "   Run: ./gradlew clean build --no-daemon"
fi

# 7. Create IDE configurations
echo ""
echo -e "${YELLOW}💻 IDE Configuration...${NC}"

# Create IntelliJ IDEA run configuration
IDEA_CONFIG_DIR=".idea/runConfigurations"
if [ ! -d "$IDEA_CONFIG_DIR" ]; then
    mkdir -p "$IDEA_CONFIG_DIR"
    cat > "$IDEA_CONFIG_DIR/Local Development.xml" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Local Development" type="SpringBootApplicationConfigurationType" factoryName="Spring Boot">
    <option name="SPRING_BOOT_MAIN_CLASS" value="org.sp.payroll_service.PayrollServiceApplication" />
    <module name="payroll-service.main" />
    <option name="PROGRAM_PARAMETERS" value="--spring.profiles.active=dev" />
    <option name="WORKING_DIRECTORY" value="$PROJECT_DIR$" />
    <method v="2">
      <option name="RunConfigurationTask" runConfigurationName="Build" />
    </method>
  </configuration>
</component>
EOF
    echo -e "${GREEN}✅ IntelliJ IDEA configuration created${NC}"
fi

# 8. Create VS Code settings
VSCODE_DIR=".vscode"
if [ ! -d "$VSCODE_DIR" ]; then
    mkdir -p "$VSCODE_DIR"
    
    # settings.json
    cat > "$VSCODE_DIR/settings.json" <<'EOF'
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.completion.importOrder": ["java", "javax", "org", "com"],
    "java.format.enabled": true,
    "java.format.settings.url": ".eclipse-formatter.xml",
    "editor.formatOnSave": true,
    "[java]": {
        "editor.defaultFormatter": "redhat.java",
        "editor.formatOnSave": true,
        "editor.codeActionsOnSave": {
            "source.fixAll": "explicit",
            "source.organizeImports": "explicit"
        }
    },
    "java.test.config": {
        "name": "Current File",
        "workingDirectory": "${workspaceRoot}",
        "args": "",
        "vmargs": [],
        "env": {}
    }
}
EOF

    # launch.json
    cat > "$VSCODE_DIR/launch.json" <<'EOF'
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Spring Boot App (Debug)",
            "request": "launch",
            "mainClass": "org.sp.payroll_service.PayrollServiceApplication",
            "cwd": "${workspaceFolder}",
            "args": "--spring.profiles.active=dev",
            "env": {
                "SPRING_PROFILES_ACTIVE": "dev"
            }
        }
    ]
}
EOF
    
    echo -e "${GREEN}✅ VS Code configuration created${NC}"
fi

# 9. Git configuration
echo ""
echo -e "${YELLOW}📌 Git configuration...${NC}"

# Configure git to use LF for line endings
git config core.safecrlf true
git config core.autocrlf input
echo -e "${GREEN}✅ Git configured (LF line endings)${NC}"

# Summary
echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ CI/CD Development Setup Complete!${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}📋 Next steps:${NC}"
echo ""
echo "1. 🔐 Configure secrets in GitHub:"
echo "   Repository → Settings → Secrets and variables → Actions"
echo ""
echo "2. 📝 Update .env.local with your configuration"
echo "   cp .env.local.example .env.local (if template exists)"
echo ""
echo "3. 🏗️  Build the project:"
echo "   ./gradlew clean build"
echo ""
echo "4. 🚀 Start development:"
echo "   ./scripts/start-debug.sh (Linux/Mac)"
echo "   ./scripts/start-debug.bat (Windows)"
echo ""
echo "5. 📚 Review CI/CD documentation:"
echo "   docs/CI-CD-PLAN.md"
echo "   .github/GITHUB_ACTIONS_SETUP.md"
echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
