// jenkins/shared-library/vars/buildApp.groovy
// Shared library function for building the application

def call(Map config = [:]) {
    echo "🏗️ Building application with Gradle..."
    
    def gradleCmd = './gradlew clean build'
    
    if (config.skipTests) {
        gradleCmd += ' -x test'
    }
    
    gradleCmd += ' --no-daemon --info'
    
    try {
        sh gradleCmd
        echo '✅ Build successful'
        return true
    } catch (Exception e) {
        echo '❌ Build failed: ' + e.message
        return false
    }
}
