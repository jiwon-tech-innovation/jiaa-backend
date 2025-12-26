pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Unit Test') {
            steps {
                echo '유닛 테스트를 실행합니다... '
                sh 'chmod +x gradlew'
                sh './gradlew bootJar --no-daemon'
            }
        }
    }
}
