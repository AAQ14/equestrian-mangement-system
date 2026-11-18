pipeline {
    agent any

    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        DOCKERHUB_USER = 'amnaaq23'
        VERSION        = "1.0.${BUILD_NUMBER}"
        EC2_HOST       = 'ubuntu@YOUR_EC2_PUBLIC_IP'
        SERVICES       = 'user-service booking-service tournament-service notification-service'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    for (s in SERVICES.split(' ')) {
                        dir("services/${s}") {
                            sh 'chmod +x mvnw && ./mvnw -B -q package'
                        }
                    }
                }
            }
        }

        stage('Build Docker images') {
            steps {
                script {
                    for (s in SERVICES.split(' ')) {
                        sh "docker build --build-arg SERVICE=${s} -t ${DOCKERHUB_USER}/equestrian-${s}:${VERSION} -t ${DOCKERHUB_USER}/equestrian-${s}:latest ."
                    }
                }
            }
        }

        stage('Push images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_TOKEN')]) {
                    sh 'echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin'
                    script {
                        for (s in SERVICES.split(' ')) {
                            sh "docker push ${DOCKERHUB_USER}/equestrian-${s}:${VERSION}"
                            sh "docker push ${DOCKERHUB_USER}/equestrian-${s}:latest"
                        }
                    }
                    sh 'docker logout'
                }
            }
        }

        stage('Deploy to AWS') {
            // Skipped until the real EC2 IP is filled in.
            when { expression { !env.EC2_HOST.contains('YOUR_EC2') } }
            steps {
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_HOST} '
                            cd equestrian &&
                            git pull &&
                            export TAG=${VERSION} &&
                            docker compose pull &&
                            docker compose up -d
                        '
                    """
                }
            }
        }
    }

    post {
        success { echo "Version ${VERSION} built, pushed and deployed." }
        failure { echo 'Pipeline failed. Check the logs above.' }
    }
}