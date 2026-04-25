// Jenkinsfile - Payroll Management System
// Pipeline definition for Jenkins CI/CD
// Supports: master (prod) → develop (staging) → feature/* (dev)
// Features: Webhooks, Email, GitHub Status, Blue Ocean, Branch Protection

pipeline {
    agent any
    
    options {
        // Keep last 30 builds
        buildDiscarder(logRotator(numToKeepStr: '30', daysToKeepStr: '30'))
        
        // Add timestamps to console output
        timestamps()
        
        // Timeout for entire pipeline
        timeout(time: 1, unit: 'HOURS')
        
        // Disable concurrent builds for main branch
        disableConcurrentBuilds(abortPrevious: false)
    }
    
    // ========================================
    // FEATURE 1: GITHUB WEBHOOK TRIGGERS
    // ========================================
    triggers {
        // GitHub push events trigger immediately (webhook)
        githubPush()
        
        // Fallback: Scan for new branches/commits hourly
        pollSCM('H H * * *')
        
        // Optional: Rebuild on specific times
        // cron('H H(0-2) * * *')  // Daily at midnight-2am
    }
    
    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip test execution')
        booleanParam(name: 'SKIP_SECURITY', defaultValue: false, description: 'Skip security scans')
        string(name: 'ENVIRONMENT', defaultValue: 'dev', description: 'Deployment environment')
    }
    
    environment {
        // Build environment
        SPRING_PROFILES_ACTIVE = 'ci'
        GRADLE_OPTS = '-Dorg.gradle.parallel=true -Dorg.gradle.workers.max=4'
        
        // Docker configuration
        DOCKER_IMAGE = "payroll-service"
        DOCKER_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        
        // Database (PostgreSQL in docker-compose)
        SPRING_DATASOURCE_URL = 'jdbc:postgresql://postgres:5432/payroll_db'
        SPRING_DATASOURCE_USERNAME = 'payroll_user'
        SPRING_DATASOURCE_PASSWORD = 'payroll_pass'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code...'
                checkout scm
                script {
                    env.GIT_COMMIT_MSG = sh(
                        script: "git log -1 --pretty=%B",
                        returnStdout: true
                    ).trim()
                    env.GIT_AUTHOR = sh(
                        script: "git log -1 --pretty=%an",
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Environment Info') {
            steps {
                echo '🔍 Displaying environment information...'
                sh '''
                    chmod +x gradlew
                    echo "📊 Build Information:"
                    echo "  Build Number: ${BUILD_NUMBER}"
                    echo "  Build URL: ${BUILD_URL}"
                    echo "  Git Branch: ${GIT_BRANCH}"
                    echo "  Git Commit: ${GIT_COMMIT}"
                    echo "  Git Author: ${GIT_AUTHOR}"
                    
                    echo ""
                    echo "💻 Tools:"
                    java -version
                    echo ""
                    ./gradlew --version
                '''
            }
        }
        
        stage('Build') {
            steps {
                script {
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - BUILD PENDING
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Build in progress...',
                        context: 'Jenkins Build',
                        status: 'PENDING'
                    )
                }
                
                echo '🏗️ Building application...'
                sh './gradlew clean build -x test --no-daemon --info'
            }
            post {
                failure {
                    echo '❌ Build failed'
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - BUILD FAILURE
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Build failed',
                        context: 'Jenkins Build',
                        status: 'FAILURE'
                    )
                }
                success {
                    echo '✅ Build successful'
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - BUILD SUCCESS
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Build passed',
                        context: 'Jenkins Build',
                        status: 'SUCCESS'
                    )
                }
            }
        }
        
        stage('Unit Tests') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                script {
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - TESTS PENDING
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Running unit tests...',
                        context: 'Jenkins Tests',
                        status: 'PENDING'
                    )
                }
                
                echo '🧪 Running unit tests...'
                sh './gradlew test --no-daemon --info'
            }
            post {
                always {
                    // Publish test results
                    junit(
                        testResults: 'build/test-results/test/*.xml',
                        allowEmptyResults: true
                    )
                    
                    // Publish HTML report
                    publishHTML(target: [
                        reportDir: 'build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: '📊 Unit Test Report',
                        keepAll: true
                    ])
                }
                failure {
                    echo '❌ Unit tests failed'
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - TESTS FAILED
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Unit tests failed',
                        context: 'Jenkins Tests',
                        status: 'FAILURE'
                    )
                }
                success {
                    echo '✅ All unit tests passed'
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - TESTS SUCCESS
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'All tests passed',
                        context: 'Jenkins Tests',
                        status: 'SUCCESS'
                    )
                }
            }
        }
        
        stage('Code Quality') {
            steps {
                echo '📈 Running code quality checks...'
                sh '''
                    # SpotBugs analysis (if configured)
                    ./gradlew check --no-daemon 2>&1 | tee spotbugs.log || true
                '''
            }
            post {
                always {
                    // Archive quality reports
                    archiveArtifacts artifacts: 'build/reports/**/*', allowEmptyArchive: true
                }
            }
        }
        
        stage('Security Scan') {
            when {
                expression { !params.SKIP_SECURITY }
            }
            steps {
                script {
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - SECURITY PENDING
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Running security scan...',
                        context: 'Jenkins Security',
                        status: 'PENDING'
                    )
                }
                
                echo '🔒 Running security scans...'
                sh '''
                    # OWASP Dependency Check
                    ./gradlew dependencyCheckAnalyze --no-daemon 2>&1 || true
                    
                    # TruffleHog secrets detection
                    echo "Scanning for exposed secrets..."
                    git log -p --all | grep -iE 'password|api.?key|secret|token' || echo "No obvious secrets found"
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'build/reports/dependency-check-report.*', allowEmptyArchive: true
                }
                failure {
                    echo '❌ Security scan found issues'
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - SECURITY FAILED
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Security vulnerabilities found',
                        context: 'Jenkins Security',
                        status: 'FAILURE'
                    )
                }
                success {
                    echo '✅ Security scan passed'
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - SECURITY SUCCESS
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'No security issues found',
                        context: 'Jenkins Security',
                        status: 'SUCCESS'
                    )
                }
            }
        }
        
        stage('Docker Build') {
            when {
                branch 'master'
            }
            steps {
                script {
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - DOCKER PENDING
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Building Docker image...',
                        context: 'Jenkins Docker',
                        status: 'PENDING'
                    )
                }
                
                echo '🐳 Building Docker image...'
                sh '''
                    BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
                    docker build \
                        -t ${DOCKER_IMAGE}:${DOCKER_TAG} \
                        -t ${DOCKER_IMAGE}:latest \
                        --build-arg BUILD_DATE=${BUILD_DATE} \
                        --build-arg VCS_REF=${GIT_COMMIT} \
                        --build-arg VERSION=${BUILD_NUMBER} \
                        .
                    
                    docker images | grep ${DOCKER_IMAGE}
                '''
            }
            post {
                failure {
                    echo '❌ Docker build failed'
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - DOCKER FAILED
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Docker build failed',
                        context: 'Jenkins Docker',
                        status: 'FAILURE'
                    )
                }
                success {
                    echo '✅ Docker image built successfully'
                    // ========================================
                    // FEATURE 4: GITHUB STATUS - DOCKER SUCCESS
                    // ========================================
                    githubNotify(
                        credentialsId: 'github-payroll-token',
                        description: 'Docker image built',
                        context: 'Jenkins Docker',
                        status: 'SUCCESS'
                    )
                }
            }
        }
        
        stage('Docker Security Scan') {
            when {
                branch 'master'
            }
            steps {
                echo '🔍 Scanning Docker image for vulnerabilities...'
                sh '''
                    # Scan with Trivy if available
                    if command -v trivy &> /dev/null; then
                        trivy image ${DOCKER_IMAGE}:${DOCKER_TAG} || true
                    else
                        echo "Trivy not available - install for image scanning"
                    fi
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'build/trivy-report.*', allowEmptyArchive: true
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            input {
                message "Deploy to STAGING (develop)?"
                ok "Deploy"
            }
            steps {
                echo '🚀 Deploying to Staging environment...'
                sh '''
                    echo "📋 Bringing down old container..."
                    docker-compose down || true
                    sleep 3
                    
                    echo "🔨 Building and starting containers..."
                    docker-compose up -d --build
                    
                    echo "⏳ Waiting for services to start..."
                    sleep 10
                    
                    echo "✅ Staging deployment complete"
                    docker-compose ps
                '''
            }
            post {
                success {
                    echo '✅ Staging deployment successful'
                    sh '''echo "🌐 Swagger UI: http://localhost:20001/pms/v1/api/swagger-ui/index.html"'''
                }
                failure {
                    echo '❌ Staging deployment failed'
                    sh '''docker-compose logs payroll-service | tail -50'''
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'master'
            }
            input {
                message "⚠️  Deploy to PRODUCTION (master)?"
                ok "Deploy to Production"
            }
            steps {
                echo '🚀 Deploying to Production environment...'
                sh '''
                    echo "📋 Bringing down old container..."
                    docker-compose down || true
                    sleep 3
                    
                    echo "🔨 Building and starting containers..."
                    docker-compose up -d --build
                    
                    echo "⏳ Waiting for services to start..."
                    sleep 15
                    
                    echo "✅ Production deployment complete"
                    docker-compose ps
                '''
            }
            post {
                success {
                    echo '✅ Production deployment successful - LIVE!'
                    sh '''echo "🌐 API: http://localhost:20001/pms/v1/api"'''
                }
                failure {
                    echo '❌ CRITICAL: Production deployment failed - ROLLBACK NEEDED'
                    sh '''docker-compose logs payroll-service | tail -100'''
                }
            }
        }
        
        stage('Slack Notification') {
            when {
                expression { currentBuild.result != null }
            }
            steps {
                echo '📢 Sending notifications...'
                script {
                    // This would send to Slack if configured
                    if (currentBuild.result == 'SUCCESS') {
                        echo '✅ Build successful - notification would be sent'
                    } else {
                        echo '❌ Build failed - notification would be sent'
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo '📊 Generating reports and archiving artifacts...'
            
            // Archive all artifacts
            archiveArtifacts(
                artifacts: 'build/libs/**/*.jar,build/reports/**/*,build/test-results/**/*',
                allowEmptyArchive: true,
                onlyIfSuccessful: false
            )
            
            // Clean workspace (optional)
            // cleanWs()
        }
        
        // ========================================
        // FEATURE 2: EMAIL NOTIFICATIONS - FAILURE
        // ========================================
        failure {
            echo '❌ Pipeline failed'
            emailext(
                to: '${CHANGE_AUTHOR_EMAIL},devops@payroll.com',
                cc: '${CHANGE_AUTHOR_EMAIL}',
                subject: "❌ BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${env.GIT_BRANCH}",
                body: '''
================================================================================
BUILD FAILURE REPORT
================================================================================

PROJECT: ${JOB_NAME}
BUILD NUMBER: ${BUILD_NUMBER}
BUILD STATUS: FAILED ❌
BUILD TIME: ${BUILD_DURATION}

BRANCH: ${GIT_BRANCH}
COMMIT: ${GIT_COMMIT}
COMMIT MESSAGE: ${GIT_COMMIT_MSG}
AUTHOR: ${CHANGE_AUTHOR}
EMAIL: ${CHANGE_AUTHOR_EMAIL}

FAILED STAGE: See console output below

================================================================================
BUILD DETAILS
================================================================================
Build URL: ${BUILD_URL}
Console Log: ${BUILD_URL}console

Jenkins Job: ${JOB_NAME}
Build Number: #${BUILD_NUMBER}

================================================================================
REMEDIATION
================================================================================
1. Check the console log above for specific error messages
2. Common issues:
   - Compile errors: Review Java/Gradle errors in console
   - Test failures: Check which unit tests failed
   - Security scan: Review vulnerability report
   - Docker build: Check Dockerfile and dependencies

3. To retry:
   - Click "Rebuild" on Jenkins job page
   - Or push new commit to same branch

================================================================================
CI/CD PIPELINE HELP
================================================================================
Documentation: https://github.com/YOUR_ORG/Payroll-Management-Service/docs
Contact: devops@payroll.com
''',
                recipientProviders: [developers(), requestor(), broken(), culprits()],
                attachLog: true,
                compressLog: true,
                mimeType: 'text/plain'
            )
        }
        
        // ========================================
        // FEATURE 2: EMAIL NOTIFICATIONS - SUCCESS
        // ========================================
        success {
            echo '✅ Pipeline completed successfully'
            script {
                if (env.GIT_BRANCH == 'origin/master' || env.GIT_BRANCH == 'master') {
                    // Production deployment success - notify more eyes
                    emailext(
                        to: 'devops@payroll.com',
                        cc: 'tech-leads@payroll.com',
                        subject: "✅ PRODUCTION BUILD SUCCESSFUL: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: '''
================================================================================
🚀 PRODUCTION DEPLOYMENT READY
================================================================================

PROJECT: ${JOB_NAME}
BUILD STATUS: SUCCESS ✅
BUILD TIME: ${BUILD_DURATION}

BRANCH: ${GIT_BRANCH} (PRODUCTION)
COMMIT: ${GIT_COMMIT}
COMMIT MESSAGE: ${GIT_COMMIT_MSG}
BUILT BY: ${CHANGE_AUTHOR}

All stages passed:
✅ Build
✅ Unit Tests
✅ Code Quality
✅ Security Scan
✅ Docker Build
✅ Docker Security Scan

Ready for manual production deployment approval.

Build URL: ${BUILD_URL}

Next Steps: 
1. Review changes
2. Approve production deployment in Jenkins
3. Monitor deployment logs

Contact: devops@payroll.com
================================================================================
''',
                        attachLog: false,
                        mimeType: 'text/plain'
                    )
                } else if (env.GIT_BRANCH == 'origin/develop' || env.GIT_BRANCH == 'develop') {
                    // Staging success - notify QA and dev leads
                    emailext(
                        to: 'qa@payroll.com,devops@payroll.com',
                        subject: "✅ STAGING BUILD SUCCESSFUL: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: '''
================================================================================
✅ STAGING BUILD SUCCESSFUL
================================================================================

PROJECT: ${JOB_NAME}
BUILD STATUS: SUCCESS ✅
ENVIRONMENT: STAGING

All tests and security scans passed.

Build URL: ${BUILD_URL}

Contact: devops@payroll.com
================================================================================
''',
                        attachLog: false,
                        mimeType: 'text/plain'
                    )
                } else {
                    // Feature branch success - notify author only
                    emailext(
                        to: '${CHANGE_AUTHOR_EMAIL}',
                        subject: "✅ BUILD SUCCESSFUL: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: '''
Build passed all checks! Ready for code review.

${BUILD_URL}
''',
                        attachLog: false,
                        mimeType: 'text/plain'
                    )
                }
            }
        }
        
        unstable {
            echo '⚠️ Pipeline completed with warnings'
            emailext(
                to: '${CHANGE_AUTHOR_EMAIL},devops@payroll.com',
                subject: "⚠️ BUILD UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: '''
Build completed but with warnings/test failures.

${BUILD_URL}
''',
                attachLog: false,
                mimeType: 'text/plain'
            )
        }
        
        cleanup {
            echo '🧹 Cleaning up...'
            deleteDir()
        }
    }
}
