# Docker 빌드 가이드

## Dockerfile 구조

현재 프로젝트는 **각 서비스 폴더의 Dockerfile**을 사용합니다.

### 서비스별 Dockerfile (권장)

각 서비스 폴더에 있는 Dockerfile을 사용합니다:

```
backend/
├── gateway-service/
│   └── Dockerfile          # ✅ 사용됨
├── auth-service/
│   └── Dockerfile          # ✅ 사용됨
├── user-service/
│   └── Dockerfile          # ✅ 사용됨
└── ...
```

**장점:**
- 서비스별 독립적인 빌드
- 각 서비스의 특성에 맞게 커스터마이징 가능
- 빌드 컨텍스트가 작아서 빠름

**사용 방법:**
```bash
# 로컬 빌드
./scripts/build-local.sh

# Jenkins
# 각 서비스 폴더에서 docker build .
```

### 루트 Dockerfile (레거시)

루트에 있는 `Dockerfile`은 **현재 사용되지 않습니다**.

**특징:**
- 멀티스테이지 빌드
- MODULE 빌드 인자로 특정 서비스만 빌드
- 전체 프로젝트를 컨텍스트로 사용

**사용 방법 (참고용):**
```bash
# 특정 서비스 빌드
docker build --build-arg MODULE=gateway-service -t gateway-service:local .
```

**제거 가능:** 현재 사용되지 않으므로 제거해도 됩니다.

### Dockerfile.local (레거시)

루트에 있는 `Dockerfile.local`은 **현재 사용되지 않습니다**.

**특징:**
- 호스트에서 미리 빌드된 JAR 사용
- 경량 이미지 (Alpine 기반)

**제거 가능:** 현재 사용되지 않으므로 제거해도 됩니다.

## 빌드 프로세스

### 1. 로컬 빌드

```bash
cd backend
./scripts/build-local.sh
```

**프로세스:**
1. 호스트에서 Gradle로 모든 JAR 빌드
2. 각 서비스 폴더의 Dockerfile로 이미지 생성

### 2. Jenkins 빌드

```groovy
// Jenkinsfile
dir("${params.SERVICE_NAME}") {
    sh "../gradlew :${params.SERVICE_NAME}:bootJar --no-daemon -x test"
    sh "docker build -t jiaa-${params.SERVICE_NAME}:${env.BUILD_NUMBER} ."
}
```

**프로세스:**
1. 각 서비스 폴더에서 Gradle 빌드
2. 각 서비스 폴더의 Dockerfile로 이미지 생성

## 각 서비스 Dockerfile 구조

모든 서비스의 Dockerfile은 동일한 구조를 사용합니다:

```dockerfile
FROM amazoncorretto:21-al2023-headless

WORKDIR /app

# 패키지 업데이트
RUN dnf update -y && dnf clean all

# 빌드된 JAR 복사
COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**빌드 전제조건:**
- 각 서비스 폴더의 `build/libs/`에 JAR 파일이 있어야 함
- `./gradlew :service-name:bootJar`로 미리 빌드 필요

## 레거시 파일 정리

다음 파일들은 제거해도 됩니다:
- `backend/Dockerfile` (루트)
- `backend/Dockerfile.local` (루트)

또는 레거시로 보관하고 싶다면 `docs/` 폴더로 이동할 수 있습니다.

