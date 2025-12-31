pipeline {
    agent any
    
    tools {
        jdk 'JDK21_corretto'
    }
    
    parameters {
        choice(name: 'SERVICE_NAME', 
               choices: ['user-service', 'auth-service', 'analysis-service', 'goal-service', 'gateway-service'], 
               description: '수동 빌드 시 서비스를 선택하세요 (웹훅 트리거 시 자동 무시됨)')
    }
    
    environment {
        ECR_REGISTRY = '541673202749.dkr.ecr.ap-northeast-2.amazonaws.com'
    }

    stages {
        // 🕵️‍♂️ [Step 0] 변경 감지 - 여러 서비스 동시 감지
        stage('Detect Changes') {
            steps {
                script {
                    def allServices = ['user-service', 'auth-service', 'analysis-service', 'goal-service', 'gateway-service']
                    def changedServices = []
                    
                    // 빌드 원인 확인
                    def causes = currentBuild.getBuildCauses()
                    def isManual = causes.any { it.shortDescription.contains("Started by user") }
                    
                    if (isManual) {
                        echo "👤 사용자 수동 실행! 선택값(${params.SERVICE_NAME})을 사용합니다."
                        changedServices = [params.SERVICE_NAME]
                    } else {
                        echo "🤖 웹훅 트리거 감지! 변경 분석 시작..."
                        try {
                            def changedFiles = sh(script: "git diff --name-only --color=never HEAD~1 HEAD", returnStdout: true).trim()
                            echo "📝 변경된 파일 목록:\n${changedFiles}"
                            
                            // 각 서비스별로 변경 여부 확인
                            for (service in allServices) {
                                if (changedFiles.contains("${service}/")) {
                                    changedServices.add(service)
                                    echo "✅ ${service} 변경 감지!"
                                }
                            }
                            
                            if (changedServices.isEmpty()) {
                                echo "⚠️ 서비스 폴더 변경 없음. 기본값(${params.SERVICE_NAME}) 사용."
                                changedServices = [params.SERVICE_NAME]
                            }
                        } catch (Exception e) {
                            echo "⚠️ Git Diff 실패. 기본값 사용: ${e.message}"
                            changedServices = [params.SERVICE_NAME]
                        }
                    }
                    
                    // 결과 저장
                    env.CHANGED_SERVICES = changedServices.join(',')
                    echo "🎯 [최종 확정] 빌드 대상 서비스들: ${env.CHANGED_SERVICES}"
                }
            }
        }

        // 🔄 [Step 1-4] 각 서비스별 순차 빌드
        stage('Build Services') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    
                    for (svc in services) {
                        def serviceName = svc.trim()
                        echo "========================================"
                        echo "🚀 [${serviceName}] 빌드 시작"
                        echo "========================================"
                        
                        // Step 1: Unit Test
                        stage("Test: ${serviceName}") {
                            echo "=== [Step 1] ${serviceName} 유닛 테스트 ==="
                            dir(serviceName) {
                                sh "chmod +x ../gradlew"
                                sh "../gradlew :${serviceName}:test --no-daemon"
                            }
                        }
                        
                        // Step 2: Source Build
                        stage("Build: ${serviceName}") {
                            echo "=== [Step 2] ${serviceName} 소스 빌드 (JAR 생성) ==="
                            dir(serviceName) {
                                sh "../gradlew :${serviceName}:bootJar --no-daemon -x test"
                            }
                            stash name: "artifacts-${serviceName}", includes: "${serviceName}/build/libs/*.jar"
                        }
                        
                        echo "✅ [${serviceName}] JAR 빌드 완료!"
                    }
                }
            }
            post {
                always {
                    script {
                        def services = env.CHANGED_SERVICES.split(',')
                        for (svc in services) {
                            def serviceName = svc.trim()
                            junit allowEmptyResults: true, testResults: "${serviceName}/build/test-results/test/*.xml"
                        }
                    }
                }
            }
        }

        // 🔍 [Step 3] Trivy 취약점 스캔
        stage('Vulnerability Scan') {
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
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    for (svc in services) {
                        def serviceName = svc.trim()
                        container('trivy') {
                            echo "=== [Step 3] ${serviceName} 파일 시스템 취약점 스캔 ==="
                            sh """
                                trivy fs --exit-code 1 --severity HIGH,CRITICAL \
                                --skip-dirs 'build' --skip-dirs '.gradle' \
                                ${serviceName}/
                            """
                        }
                    }
                }
            }
        }

        // 🐳 [Step 4] Kaniko 이미지 빌드 & ECR Push
        stage('Docker Build & Push') {
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
    
  initContainers:
  - name: kaniko-init
    image: gcr.io/kaniko-project/executor:debug
    command: ["/busybox/sh", "-c"]
    args: ["cp -a /kaniko/* /kaniko-shared/"]
    volumeMounts:
    - name: kaniko-bin
      mountPath: /kaniko-shared

  containers:
  - name: kaniko
    image: alpine:latest
    command: ["cat"]
    tty: true
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
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    
                    for (svc in services) {
                        def serviceName = svc.trim()
                        def ecrRepository = "jiaa/${serviceName}"
                        
                        container('kaniko') {
                            echo "=== [Step 4] ${serviceName} Docker 이미지 빌드 & Push ==="
                            
                            // JAR 파일 가져오기
                            unstash "artifacts-${serviceName}"
                            
                            // 파일 확인
                            sh "ls -al ${serviceName}/build/libs/"
                            
                            // Kaniko 실행
                            sh """
                                /kaniko/executor \\
                                --context=dir://${env.WORKSPACE} \\
                                --dockerfile=${env.WORKSPACE}/${serviceName}/Dockerfile \\
                                --destination=${ECR_REGISTRY}/${ecrRepository}:${env.BUILD_NUMBER} \\
                                --destination=${ECR_REGISTRY}/${ecrRepository}:latest \\
                                --ignore-path=/var/spool/mail \\
                                --force
                            """
                            
                            echo "✅ [${serviceName}] ECR Push 완료!"
                        }
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo "🎉 모든 서비스 빌드 & 배포 성공!"
            echo "빌드된 서비스: ${env.CHANGED_SERVICES}"
        }
        failure {
            echo "❌ 빌드 실패. 로그를 확인하세요."
        }
    }
}
