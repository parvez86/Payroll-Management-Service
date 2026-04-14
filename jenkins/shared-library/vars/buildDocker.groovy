// jenkins/shared-library/vars/buildDocker.groovy
// Shared library function for Docker image building

def call(Map config = [:]) {
    echo "🐳 Building Docker image..."
    
    def imageName = config.imageName ?: 'payroll-service'
    def tag = config.tag ?: "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    
    try {
        sh '''
            docker build \
                -t ${imageName}:${tag} \
                -t ${imageName}:latest \
                -f ''' + dockerfile + ''' \
                --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
                --build-arg VCS_REF=${GIT_COMMIT} \
                --build-arg VERSION=${BUILD_NUMBER} \
                .
            
            docker images | grep ${imageName}
        '''
        echo '✅ Docker image built successfully'
        return true
    } catch (Exception e) {
        echo '❌ Docker build failed: ' + e.message
        return false
    }
}
