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
                // 1. 먼저 테스트만 돌려서 통과 여부를 확인
                sh './gradlew test --no-daemon' 
            }
            post {
                always {
                    // 테스트 결과를 젠킨스 대시보드에 기록
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }
    }
}
