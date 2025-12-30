pipeline {
    agent any
    
    tools {
        // Jenkins 설정에 있는 이름과 정확히 일치해야 합니다.
        jdk 'JDK21_corretto'
    }
    
    parameters {
        choice(name: 'SERVICE_NAME', 
               choices: ['user-service', 'auth-service', 'analysis-service', 'goal-service', 'gateway-service'], 
               description: '빌드할 서비스를 선택하세요')
    }

    stages {
        stage('Unit Test') {
            steps {
                echo "=== [Step 1] ${params.SERVICE_NAME} 유닛 테스트 ==="
                dir("${params.SERVICE_NAME}") {
                    sh "chmod +x ../gradlew"
                    sh 'java -version'
                    sh "../gradlew :${params.SERVICE_NAME}:test --no-daemon"
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${params.SERVICE_NAME}/build/test-results/test/*.xml"
                }
            }
        }

        stage('Source Build') {
            steps {
                echo "=== [Step 2] ${params.SERVICE_NAME} 소스 빌드 (JAR 생성) ==="
                dir("${params.SERVICE_NAME}") {
                    sh "../gradlew :${params.SERVICE_NAME}:bootJar --no-daemon -x test"
                }
                stash name: 'build-artifacts', includes: "${params.SERVICE_NAME}/build/libs/*.jar"
            }
        }

        stage('Vulnerability Scan (FS)') {
            agent {
                kubernetes {
                    yaml '''
apiVersion: v1
kind: Pod
spec:
  tolerations:
  - key: "jiaa.io/system-node"
    operator: "Exists"
    effect: "NoSchedule"
  containers:
  - name: trivy
    image: aquasec/trivy:latest
    command: ["cat"]
    tty: true
'''
                }
            }
            steps {
                container('trivy') {
                    echo "=== [Step 3] 파일 시스템 취약점 스캔 ==="
                    sh """
                        trivy fs --exit-code 1 --severity HIGH,CRITICAL \
                        --skip-dirs 'build' --skip-dirs '.gradle' \
                        ${params.SERVICE_NAME}/
                    """
                }
            }
        }

        stage('Build & Push with Kaniko') {
            agent {
                kubernetes {
                    yaml """
apiVersion: v1
kind: Pod
spec:
  tolerations:
  - key: "jiaa.io/system-node"
    operator: "Exists"
    effect: "NoSchedule"
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    # 👇 [핵심 변경] 복잡한 스크립트 제거! 그냥 켜놓기만 합니다. (무한 대기)
    command: ["/busybox/sh", "-c", "cat"]
    tty: true
    resources:
      requests:
        memory: "1Gi"
        cpu: "500m"
      limits:
        memory: "2Gi"
        cpu: "1"
    volumeMounts:
    - name: kaniko-secret
      mountPath: /kaniko/.docker
  volumes:
  - name: kaniko-secret
    secret:
      secretName: ecr-credentials
      items:
        - key: .dockerconfigjson
          path: config.json
"""
                }
            }
            environment {
                // 👇 본인의 ECR 주소가 맞는지 다시 한번 확인하세요!
                ECR_REGISTRY = '541673202749.dkr.ecr.ap-northeast-2.amazonaws.com'
                ECR_REPOSITORY = "jiaa/${params.SERVICE_NAME}"
            }
            steps {
                container('kaniko') {
                    echo "=== [Step 4] Kaniko 이미지 빌드 및 배포 ==="
                    
                    // 1. 빌드한 JAR 파일 가져오기
                    unstash 'build-artifacts'
                    
                    // 2. 파일 잘 왔나 확인 (디버깅용)
                    sh "ls -al ${params.SERVICE_NAME}/build/libs/"
                    
                    // 3. Kaniko 실행 (젠킨스가 직접 명령을 내립니다)
                    // context와 dockerfile 경로에 env.WORKSPACE를 사용하여 절대경로를 줍니다.
                    sh """
                        /kaniko/executor \
                        --context=dir://${env.WORKSPACE} \
                        --dockerfile=${env.WORKSPACE}/${params.SERVICE_NAME}/Dockerfile \
                        --destination=${ECR_REGISTRY}/${ECR_REPOSITORY}:${env.BUILD_NUMBER} \
                        --destination=${ECR_REGISTRY}/${ECR_REPOSITORY}:latest \
                        --force
                    """
                }
            }
        }
    }
}
