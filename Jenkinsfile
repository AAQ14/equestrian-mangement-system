pipeline {
    agent any

    // Check GitHub for new commits every 2 minutes.
    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        EC2_HOST = 'ubuntu@YOUR_EC2_PUBLIC_IP'   // filled in after Part 2 (AWS)
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build services') {
            steps {
                script {
                    def services = ['user-service', 'booking-service', 'tournament-service', 'notification-service']
                    for (s in services) {
                        dir("services/${s}") {
                            sh 'chmod +x mvnw && ./mvnw -B -q package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Deploy to AWS') {
            when { branch 'main' }   // only deploy what's on main
            steps {
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_HOST} '
                            cd equestrian &&
                            git pull &&
                            docker compose up -d --build
                        '
                    """
                }
            }
        }
    }

    post {
        success { echo 'Built and deployed successfully.' }
        failure { echo 'Pipeline failed. Check the logs above.' }
    }
}