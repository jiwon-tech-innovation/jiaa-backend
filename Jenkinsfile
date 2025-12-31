pipeline {
    agent any
    
    tools {
        // Jenkins Global Tool Configuration에 설정된 JDK 이름
        jdk 'JDK21_corretto'
    }
    
    parameters {
        choice(name: 'SERVICE_NAME', 
               choices: ['user-service', 'auth-service', 'analysis-service', 'goal-service', 'gateway-service'], 
               description: '수동 빌드 시 서비스를 선택하세요 (웹훅 트리거 시 자동 무시됨)')
    }
    
    environment {
        ECR_REGISTRY = '541673202749.dkr.ecr.ap-northeast-2.amazonaws.com'
        // TARGET_SERVICE는 'Detect Changes' 단계에서 동적으로 설정됨
    }

    stages {
        // [Step 0] 변경 감지 탐정 단계
        // 🕵️‍♂️ [Step 0] 변경 감지 탐정 단계 (수정판)
        stage('Detect Changes') {
            steps {
                script {
                    def detectedService = params.SERVICE_NAME // 1. 일단 기본값(user-service)으로 시작
                    
                    // 2. 빌드 원인 확인
                    def causes = currentBuild.getBuildCauses()
                    def isManual = false
                    for (cause in causes) {
                        if (cause.shortDescription.contains("Started by user")) {
                            isManual = true
                        }
                    }
                    
                    if (isManual) {
                        echo "👤 사용자 수동 실행! 선택값(${detectedService})을 사용합니다."
                    } else {
                        echo "🤖 웹훅 트리거 감지! 변경 분석 시작..."
                        try {
                            // 👇 [핵심 수정] --color=never 옵션 추가 (색상 코드 제거)
                            def changedFiles = sh(script: "git diff --name-only --color=never HEAD~1 HEAD", returnStdout: true).trim()
                            echo "📝 변경된 파일 목록(Raw):\n${changedFiles}"
                            
                            // 3. 변경된 파일에 따라 서비스 교체
                            echo "DEBUG: Checking for user-service/: ${changedFiles.contains('user-service/')}"
                            echo "DEBUG: Checking for analysis-service/: ${changedFiles.contains('analysis-service/')}"
                            
                            if (changedFiles.contains("user-service/")) {
                                detectedService = "user-service"
                            } else if (changedFiles.contains("auth-service/")) {
                                detectedService = "auth-service"
                            } else if (changedFiles.contains("analysis-service/")) {
                                detectedService = "analysis-service"
                            } else if (changedFiles.contains("goal-service/")) {
                                detectedService = "goal-service"
                            } else if (changedFiles.contains("gateway-service/")) {
                                detectedService = "gateway-service"
                            } else {
                                echo "⚠️ 서비스 폴더 변경 없음. 기본값 유지."
                            }
                        } catch (Exception e) {
                            echo "⚠️ Git Diff 실패 (첫 커밋 등). 기본값 유지."
                        }
                    }
                    
                    // 4. 최종 결과를 환경 변수에 확정 저장
                    echo "DEBUG: detectedService value before save = ${detectedService}"
                    env.TARGET_SERVICE = detectedService
                    env.ECR_REPOSITORY = "jiaa/${detectedService}"
                    
                    echo "🎯 [최종 확정] 빌드 대상: ${env.TARGET_SERVICE}"
                }
            }
        }

        stage('Unit Test') {
            steps {
                echo "=== [Step 1] ${env.TARGET_SERVICE} 유닛 테스트 ==="
                dir("${env.TARGET_SERVICE}") {
                    sh "chmod +x ../gradlew"
                    sh 'java -version'
                    sh "../gradlew :${env.TARGET_SERVICE}:test --no-daemon"
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${env.TARGET_SERVICE}/build/test-results/test/*.xml"
                }
            }
        }

        stage('Source Build') {
            steps {
                echo "=== [Step 2] ${env.TARGET_SERVICE} 소스 빌드 (JAR 생성) ==="
                dir("${env.TARGET_SERVICE}") {
                    sh "../gradlew :${env.TARGET_SERVICE}:bootJar --no-daemon -x test"
                }
                // Kaniko 파드로 넘겨주기 위해 JAR 파일 저장
                stash name: 'build-artifacts', includes: "${env.TARGET_SERVICE}/build/libs/*.jar"
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
                        ${env.TARGET_SERVICE}/
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
    
  # [핵심] Kaniko 실행파일과 인증서를 공유 볼륨으로 복사하는 Init Container
  initContainers:
  - name: kaniko-init
    image: gcr.io/kaniko-project/executor:debug
    command: ["/busybox/sh", "-c"]
    args: ["cp -a /kaniko/* /kaniko-shared/"]
    volumeMounts:
    - name: kaniko-bin
      mountPath: /kaniko-shared

  containers:
  # [핵심] 젠킨스와 호환성 좋은 Busybox에서 Kaniko 실행
  - name: kaniko
    image: busybox:latest
    command: ["/bin/sh", "-c", "cat"]
    tty: true
    env:
    - name: PATH
      value: "/kaniko:/bin:/usr/bin"
    resources:
      requests:
        memory: "1Gi"
        cpu: "500m"
      limits:
        memory: "2Gi"
        cpu: "1"
    volumeMounts:
    - name: kaniko-bin
      mountPath: /kaniko
    - name: kaniko-secret
      mountPath: /kaniko/.docker
      
  volumes:
  - name: kaniko-bin
    emptyDir: {}
  - name: kaniko-secret
    secret:
      secretName: ecr-credentials
      items:
        - key: .dockerconfigjson
          path: config.json
"""
                }
            }
            steps {
                container('kaniko') {
                    echo "=== [Step 4] Kaniko 이미지 빌드 및 배포 (${env.TARGET_SERVICE}) ==="
                    
                    // 1. 아까 빌드한 JAR 파일 가져오기
                    unstash 'build-artifacts'
                    
                    // 2. 디버깅: 파일 확인
                    sh "ls -al ${env.TARGET_SERVICE}/build/libs/"
                    
                    // 3. Kaniko 실행 (Busybox 환경에서 실행됨)
                    sh """
                        /kaniko/executor \
                        --context=dir://${env.WORKSPACE} \
                        --dockerfile=${env.WORKSPACE}/${env.TARGET_SERVICE}/Dockerfile \
                        --destination=${ECR_REGISTRY}/${env.ECR_REPOSITORY}:${env.BUILD_NUMBER} \
                        --destination=${ECR_REGISTRY}/${env.ECR_REPOSITORY}:latest \
                        --force
                    """
                }
            }
        }
    }
}
