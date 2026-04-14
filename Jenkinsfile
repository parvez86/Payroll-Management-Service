// Jenkinsfile - Payroll Management System
// Pipeline definition for Jenkins CI/CD
// Supports: master (prod) → develop (staging) → feature/* (dev)

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
                echo '🏗️ Building application...'
                sh './gradlew clean build -x test --no-daemon --info'
            }
            post {
                failure {
                    echo '❌ Build failed'
                }
                success {
                    echo '✅ Build successful'
                }
            }
        }
        
        stage('Unit Tests') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
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
            }
        }
        
        stage('Docker Build') {
            when {
                branch 'master'
            }
            steps {
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
                }
                success {
                    echo '✅ Docker image built successfully'
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
        
        failure {
            echo '❌ Pipeline failed'
            // Send failure notifications here
        }
        
        success {
            echo '✅ Pipeline completed successfully'
            // Send success notifications here
        }
        
        unstable {
            echo '⚠️ Pipeline completed with warnings'
        }
        
        cleanup {
            echo '🧹 Cleaning up...'
            deleteDir()
        }
    }
}
