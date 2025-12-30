pipeline {
    agent any
    
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
  - name: jnlp
    resources:
      requests:
        memory: "256Mi"
        cpu: "100m"
    volumeMounts:
    - name: workspace
      mountPath: /workspace
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command:
    - /busybox/sh
    - -c
    - echo 'Kaniko container waiting for signal...'; while [ ! -f /workspace/.ready ]; do sleep 1; done; echo 'Signal received. Starting Kaniko build...'; /kaniko/executor --context=/workspace --dockerfile=/workspace/${params.SERVICE_NAME}/Dockerfile --destination=541673202749.dkr.ecr.ap-northeast-2.amazonaws.com/jiaa/${params.SERVICE_NAME}:${env.BUILD_NUMBER} --destination=541673202749.dkr.ecr.ap-northeast-2.amazonaws.com/jiaa/${params.SERVICE_NAME}:latest
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
    - name: workspace
      mountPath: /workspace
  volumes:
  - name: kaniko-secret
    secret:
      secretName: ecr-credentials
      items:
        - key: .dockerconfigjson
          path: config.json
  - name: workspace
    emptyDir: {}
"""
                }
            }
            environment {
                ECR_REGISTRY = '541673202749.dkr.ecr.ap-northeast-2.amazonaws.com'
                ECR_REPOSITORY = "jiaa/${params.SERVICE_NAME}"
            }
            steps {
                echo "=== [Step 4] Kaniko 이미지 빌드 & 배포 ==="
                
                // JAR 파일 unstash (jnlp 컨테이너에서 실행됨)
                unstash 'build-artifacts'
                
                // 파일을 Kaniko 공유 볼륨에 복사
                sh "cp -r . /workspace/"
                sh "ls -al /workspace/${params.SERVICE_NAME}/build/libs/"
                
                // 준비 완료 신호
                sh "touch /workspace/.ready"
                echo "Kaniko 실행 신호 보냄. (Direct YAML Args 방식)"

                // Kaniko 완료 모니터링
                script {
                    def timeout = 600 // 10분
                    def elapsed = 0
                    while (elapsed < timeout) {
                        sleep 10
                        elapsed += 10
                        echo "Kaniko 빌드 진행 중... (${elapsed}s)"
                        try {
                            def logs = containerLog('kaniko')
                            if (logs.contains('Pushing image') || logs.contains('pushed')) {
                                echo "Kaniko 빌드 완료!"
                                break
                            }
                            if (logs.contains('error') || logs.contains('Error') || logs.contains('FAILED')) {
                                echo "================ KANIKO LOGS ================"
                                echo logs
                                echo "============================================="
                                error "Kaniko 빌드 실패"
                            }
                        } catch (e) {
                            echo "로그 확인 중: ${e.message}"
                        }
                    }
                }
            }
        }
    }
}
