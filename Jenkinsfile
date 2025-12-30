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
    image: gcr.io/kaniko-project/executor:v1.23.0
    command: ["/busybox/sh", "-c"]
    args:
    - |
      echo "Waiting for source files..."
      while [ ! -f /workspace/.ready ]; do sleep 2; done
      echo "Starting Kaniko build..."
      /kaniko/executor \\
        --context=dir:///workspace \\
        --dockerfile=/workspace/${params.SERVICE_NAME}/Dockerfile \\
        --destination=${env.ECR_REGISTRY}/${env.ECR_REPOSITORY}:${env.BUILD_NUMBER} \\
        --destination=${env.ECR_REGISTRY}/${env.ECR_REPOSITORY}:latest \\
        --force
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
                echo "=== [Step 4] Kaniko 이미지 빌드 ==="
                
                // JAR 파일 unstash
                unstash 'build-artifacts'
                
                // 파일을 Kaniko 공유 볼륨에 복사
                sh "cp -r . /workspace/"
                sh "ls -al /workspace/${params.SERVICE_NAME}/build/libs/"
                
                // Kaniko에 준비 완료 신호
                sh "touch /workspace/.ready"
                echo "Kaniko 빌드 시작됨..."
                
                // Kaniko 완료까지 대기 (컨테이너 로그 확인)
                script {
                    def timeout = 600 // 10분
                    def elapsed = 0
                    while (elapsed < timeout) {
                        sleep 10
                        elapsed += 10
                        echo "Kaniko 빌드 진행 중... (${elapsed}s)"
                        def logs = containerLog('kaniko')
                        if (logs.contains('Build complete') || logs.contains('Pushing image')) {
                            echo "Kaniko 빌드 완료!"
                            break
                        }
                    }
                }
            }
        }
    }
}pipeline {
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
    command:
    - /busybox/sh
    - -c
    - |
      mkdir -p /bin
      ln -sf /busybox/sh /bin/sh
      ln -sf /busybox/cat /bin/cat
      ln -sf /busybox/ls /bin/ls
      ln -sf /busybox/cp /bin/cp
      ln -sf /busybox/mkdir /bin/mkdir
      ln -sf /busybox/rm /bin/rm
      ln -sf /busybox/mv /bin/mv
      ln -sf /busybox/sleep /bin/sleep
      ln -sf /busybox/touch /bin/touch
      ln -sf /busybox/echo /bin/echo
      ln -sf /busybox/chmod /bin/chmod
      ln -sf /busybox/pwd /bin/pwd
      ln -sf /busybox/head /bin/head
      ln -sf /busybox/tail /bin/tail
      ln -sf /busybox/ps /bin/ps
      ln -sf /busybox/kill /bin/kill
      ln -sf /busybox/sed /bin/sed
      ln -sf /busybox/grep /bin/grep
      ln -sf /busybox/find /bin/find
      ln -sf /busybox/wc /bin/wc
      ln -sf /busybox/tr /bin/tr
      ln -sf /busybox/cut /bin/cut
      ln -sf /busybox/date /bin/date
      ln -sf /busybox/mktemp /bin/mktemp
      ln -sf /busybox/base64 /bin/base64
      cat
    tty: true
    env:
    - name: PATH
      value: "/busybox:/kaniko:/bin:/usr/bin"
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
                ECR_REGISTRY = '541673202749.dkr.ecr.ap-northeast-2.amazonaws.com'
                ECR_REPOSITORY = "jiaa/${params.SERVICE_NAME}"
            }
            steps {
                container('kaniko') {
                    echo "=== [Step 4] Kaniko 이미지 빌드 및 배포 ==="
                    
                    // JAR 파일 unstash
                    unstash 'build-artifacts'
                    
                    // 파일 확인
                    sh "ls -al ${params.SERVICE_NAME}/build/libs/"
                    
                    // Kaniko 실행
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
