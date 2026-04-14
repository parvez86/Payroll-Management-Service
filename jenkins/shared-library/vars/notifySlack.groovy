// jenkins/shared-library/vars/notifySlack.groovy
// Shared library function for Slack notifications

def call(Map config = [:]) {
    def status = config.status ?: currentBuild.result
    def message = config.message ?: "Build ${status}"
    def channel = config.channel ?: '#ci-cd'
    def color = status == 'SUCCESS' ? 'good' : 'danger'
    
    echo "📢 Sending Slack notification to ${channel}..."
    
    try {
        // This would send a Slack message if webhook is configured
        // For now, just log the notification
        echo """
        Slack Notification:
          Channel: ${channel}
          Status: ${status}
          Message: ${message}
          Build: ${env.BUILD_NUMBER}
          URL: ${env.BUILD_URL}
        """
    } catch (Exception e) {
        echo "Warning: Could not send Slack notification: " + e.message
    }
}
