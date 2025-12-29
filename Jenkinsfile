pipeline {
    agent any
    
    // ⚠️ Jenkins 관리 -> Global Tool Configuration에 'JDK21_corretto'가 설정되어 있어야 합니다!
    tools {
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
                    // 테스트 실행
                    sh "../gradlew :${params.SERVICE_NAME}:test --no-daemon"
                }
            }
            post {
                always {
                    // 테스트 결과 리포트
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
                
                // 빌드된 JAR 파일을 보관함(stash)에 저장!
                // Kaniko 파드는 완전히 다른 컴퓨터라서 이걸 안 해주면 파일이 없습니다.
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
                    // Trivy는 소스코드만 보면 되니까 unstash 필요 없음 (Git Checkout은 자동)
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
                    yaml '''
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
    command: ["/busybox/sh", "-c", "mkdir -p /bin && ln -sf /busybox/sh /bin/sh && ln -sf /busybox/cat /bin/cat && ln -sf /busybox/sleep /bin/sleep && ln -sf /busybox/ls /bin/ls && cat"]
    tty: true
    env:
    - name: PATH
      value: "/busybox:/kaniko:/usr/local/bin:/usr/bin:/bin"
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
'''
                }
            }
            environment {
                // 본인의 ECR 주소 확인 필수
                ECR_REGISTRY = '541673202749.dkr.ecr.ap-northeast-2.amazonaws.com'
                ECR_REPOSITORY = "jiaa/${params.SERVICE_NAME}"
            }
            steps {
                container('kaniko') {
                    echo "=== [Step 4] Kaniko 이미지 빌드 및 배포 ==="
                    
                    // 아까 맡겨둔 JAR 파일을 여기서 찾음(unstash)!
                    unstash 'build-artifacts'
                    
                    // 파일 잘 왔는지 확인 사살 (디버깅용)
                    sh "ls -al ${params.SERVICE_NAME}/build/libs/"

                    sh """
                        /kaniko/executor \
                        --context=dir://\${WORKSPACE} \
                        --dockerfile=\${WORKSPACE}/${params.SERVICE_NAME}/Dockerfile \
                        --destination=${ECR_REGISTRY}/${ECR_REPOSITORY}:${env.BUILD_NUMBER} \
                        --destination=${ECR_REGISTRY}/${ECR_REPOSITORY}:latest \
                        --force
                    """
                }
            }
        }
    }
}
