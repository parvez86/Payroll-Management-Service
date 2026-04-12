// jenkins/shared-library/vars/runTests.groovy
// Shared library function for running tests

def call(Map config = [:]) {
    echo "🧪 Running tests with Gradle..."
    
    def gradleCmd = './gradlew test'
    
    if (config.profile) {
        gradleCmd = "SPRING_PROFILES_ACTIVE=${config.profile} " + gradleCmd
    }
    
    gradleCmd += ' --no-daemon --info'
    
    try {
        sh gradleCmd
        echo '✅ Tests passed'
        return true
    } catch (Exception e) {
        echo '⚠️ Tests failed: ' + e.message
        return false
    }
}
