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
                echo '유닛 테스트를 실행합니다... '
                sh './gradlew test --no-daemon' 
            }
            post {
                always {
                    // 테스트 결과를 젠킨스 대시보드에 기록
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Source Build') {
            steps {
                echo '소스 빌드를 실행합니다... '
                sh 'chmod +x gradlew'
                sh './gradlew bootJar --no-daemon'
            }
        }

        stage('Docker Image Build') {
            steps {
                echo '스프링 부트 이미지를 굽기 시작합니다...'
                
                // 빌드 번호를 태그로 사용해서 '고유한' 이미지를 만듬
                sh "docker build -t jiaa-spring-boot:${env.BUILD_NUMBER} ." 
                
                // 나중에 ECR 푸시를 위해 'latest' 태그도 같이 만들어두면 편함
                sh "docker tag jiaa-spring-boot:${env.BUILD_NUMBER} jiaa-spring-boot:latest"
            
            }
        }
    }
}
