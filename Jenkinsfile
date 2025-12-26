pipeline {
    agent any
    
    parameters {
        choice(name: 'SERVICE_NAME', 
               choices: ['user-service', 'auth-service', 'analysis-service', 'goal-service', 'gateway-service'], 
               description: '빌드할 서비스를 선택하세요')
    }

    stages {
        stage('Unit Test') {
            steps {
                echo "${params.SERVICE_NAME} 유닛 테스트를 시작합니다..."
                dir("${params.SERVICE_NAME}") {
                    sh "chmod +x ../gradlew"
                    sh "../gradlew :${params.SERVICE_NAME}:test --no-daemon"
                }
            }
            post {
                always {
                    junit "${params.SERVICE_NAME}/build/test-results/test/*.xml"
                }
            }
        }

        stage('Source Build') {
            steps {
                echo "${params.SERVICE_NAME} 소스 빌드를 시작합니다..."
                dir("${params.SERVICE_NAME}") {
                    // 테스트는 위에서 했으니 테스트는 건너뜀
                    sh "../gradlew :${params.SERVICE_NAME}:bootJar --no-daemon -x test"
                }
            }
        }

        stage('Docker Image Build') {
            steps {
                dir("${params.SERVICE_NAME}") {
                    echo "이미지 빌드를 시작합니다..."
                    sh "docker build -t jiaa-${params.SERVICE_NAME}:${env.BUILD_NUMBER} ." 
                    sh "docker tag jiaa-${params.SERVICE_NAME}:${env.BUILD_NUMBER} jiaa-${params.SERVICE_NAME}:latest"
                }
            }
        }

        stage('Vulnerability Scan') {
            steps {
                echo "${params.SERVICE_NAME} 이미지의 취약점을 정밀 수색합니다..."
                
                // HIGH, CRITICAL 등급의 취약점이 발견되면 빌드를 멈추게 설정할 수 있음
                // 젠킨스 서버에 trivy가 설치되어 있어야 함
                sh """
                    docker run --rm \
                    -v /var/run/docker.sock:/var/run/docker.sock \
                    -v trivy-db-cache:/root/.cache \
                    aquasec/trivy:latest image \
                    --exit-code 1 \
                    --severity HIGH,CRITICAL \
                    jiaa-${params.SERVICE_NAME}:${env.BUILD_NUMBER}
                """
            }
        }

        stage('ECR Push') {
            steps {
                echo "${params.SERVICE_NAME} 이미지를 AWS ECR로 쏘아 올립니다! 🚀"
                
                // 1. AWS ECR 로그인 (비밀번호를 가져와서 도커 로그인에 전달)
                // 주인님의 계정 ID: 541673202749, 리전: ap-northeast-2 활용 
                sh "aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 541673202749.dkr.ecr.ap-northeast-2.amazonaws.com"
                
                // 2. ECR용 태그 생성 (로컬 이미지를 ECR 창고 주소로 이름표를 바꿔줌)
                sh "docker tag jiaa-${params.SERVICE_NAME}:${env.BUILD_NUMBER} 541673202749.dkr.ecr.ap-northeast-2.amazonaws.com/jiaa-${params.SERVICE_NAME}:${env.BUILD_NUMBER}"
                sh "docker tag jiaa-${params.SERVICE_NAME}:${env.BUILD_NUMBER} 541673202749.dkr.ecr.ap-northeast-2.amazonaws.com/jiaa-${params.SERVICE_NAME}:latest"
                
                // 3. 실제 이미지 푸시! (진짜로 클라우드 창고에 집어넣는 순간)
                sh "docker push 541673202749.dkr.ecr.ap-northeast-2.amazonaws.com/jiaa-${params.SERVICE_NAME}:${env.BUILD_NUMBER}"
                sh "docker push 541673202749.dkr.ecr.ap-northeast-2.amazonaws.com/jiaa-${params.SERVICE_NAME}:latest"
            }
        }
    }
}