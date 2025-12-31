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
        
        // 초기값은 파라미터에서 가져오지만, 아래 'Detect Changes' 단계에서 덮어씌워질 수 있음
        TARGET_SERVICE = "${params.SERVICE_NAME}"
    }

    stages {
        // [Step 0] 변경 감지 탐정 단계
        stage('Detect Changes') {
            steps {
                script {
                    // 1. 빌드 원인 확인 (사람이 눌렀나? 웹훅이 찔렀나?)
                    def causes = currentBuild.getBuildCauses()
                    def isManual = false
                    
                    for (cause in causes) {
                        if (cause.shortDescription.contains("Started by user")) {
                            isManual = true
                        }
                    }
                    
                    if (isManual) {
                        echo "👤 사용자 수동 실행 감지! 선택된 서비스(${params.SERVICE_NAME})로 진행합니다."
                        env.TARGET_SERVICE = params.SERVICE_NAME
                    } else {
                        echo "🤖 웹훅(Webhook) 트리거 감지! 변경된 파일을 분석합니다..."
                        
                        try {
                            // Git Diff로 변경된 파일 목록 가져오기 (이전 커밋 vs 현재 커밋)
                            def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                            echo "📝 변경된 파일 목록:\n${changedFiles}"
                            
                            // 변경된 폴더에 따라 서비스 매칭 (우선순위 로직)
                            if (changedFiles.contains("user-service/")) {
                                env.TARGET_SERVICE = "user-service"
                            } else if (changedFiles.contains("auth-service/")) {
                                env.TARGET_SERVICE = "auth-service"
                            } else if (changedFiles.contains("analysis-service/")) {
                                env.TARGET_SERVICE = "analysis-service"
                            } else if (changedFiles.contains("goal-service/")) {
                                env.TARGET_SERVICE = "goal-service"
                            } else if (changedFiles.contains("gateway-service/")) {
                                env.TARGET_SERVICE = "gateway-service"
                            } else {
                                echo "⚠️ 특정 서비스 폴더의 변경사항을 찾지 못했습니다. (공통 모듈 수정 등). 기본값(${params.SERVICE_NAME})으로 진행합니다."
                            }
                        } catch (Exception e) {
                            echo "⚠️ 변경 내역 조회 실패 (첫 빌드일 수 있음). 기본값으로 진행합니다."
                        }
                    }
                    
                    // 최종 결정된 서비스 이름 확정
                    env.ECR_REPOSITORY = "jiaa/${env.TARGET_SERVICE}"
                    echo "🎯 최종 빌드 대상 확정: [ ${env.TARGET_SERVICE} ]"
                    echo "📦 타겟 ECR 리포지토리: [ ${env.ECR_REPOSITORY} ]"
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
