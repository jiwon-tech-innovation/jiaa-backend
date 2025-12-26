# JIAA Backend

Spring Boot 기반 마이크로서비스 아키텍처 백엔드 프로젝트

## 📋 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 25 |
| Framework | Spring Boot 4.0.1, Spring Cloud 2025.1.0 |
| Build Tool | Gradle (Kotlin DSL) |
| Database | PostgreSQL |
| Cache | Redis |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Container | Docker, Kubernetes |
| API Docs | SpringDoc OpenAPI (Swagger) |

## 🏗️ 서비스 구조

```
jiaa-backend/
├── discovery-service    # Eureka 서비스 디스커버리 (포트: 8761)
├── gateway-service      # API Gateway (포트: 8080)
├── auth-service         # 인증/인가 서비스
├── user-service         # 사용자 관리 서비스
├── goal-service         # 목표 관리 서비스
├── analysis-service     # 분석 서비스
└── common-lib           # 공통 라이브러리
```

## 🚀 로컬 개발 환경 설정

### 사전 요구사항

- **Java 25** (Eclipse Temurin 권장)
- **Docker Desktop** (Kubernetes 활성화)
- **PostgreSQL** (로컬 또는 Docker)
- **Redis** (로컬 또는 Docker)

### 1. 데이터베이스 설정

```bash
# PostgreSQL Docker 실행
docker run -d \
  --name postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=jiwon \
  -p 5432:5432 \
  postgres:16-alpine

# Redis Docker 실행
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

### 2. 프로젝트 빌드

```bash
# 전체 빌드 (테스트 제외)
./gradlew clean bootJar -x test --parallel

# 특정 서비스만 빌드
./gradlew :auth-service:bootJar -x test
```

### 3. 로컬 실행 방법

#### 방법 A: IDE에서 직접 실행

1. Discovery Service 먼저 실행
2. 나머지 서비스들 실행

각 서비스의 `*Application.java` 파일을 실행하면 됩니다.

#### 방법 B: Kubernetes (Docker Desktop)

```bash
# 1. Docker 이미지 빌드
./scripts/build-local.sh

# 2. Kubernetes 배포
./scripts/deploy-local.sh

# 3. Gateway 포트포워딩 (별도 터미널)
./scripts/port-forward.sh

# 4. 리소스 정리
./scripts/cleanup-local.sh
```

## 🔗 API 엔드포인트

### 로컬 개발 환경

| 서비스 | URL |
|--------|-----|
| Gateway (메인 진입점) | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

### Gateway를 통한 서비스 접근

```
http://localhost:8080/auth-service/**
http://localhost:8080/user-service/**
http://localhost:8080/goal-service/**
http://localhost:8080/analysis-service/**
```

## 📁 프로젝트 구조

```
backend/
├── build.gradle.kts           # 루트 빌드 설정
├── settings.gradle.kts        # 멀티 모듈 설정
├── gradle.properties          # Gradle 속성
├── Dockerfile                 # 프로덕션용 Dockerfile
├── Dockerfile.local           # 로컬 개발용 경량 Dockerfile
├── scripts/                   # 배포 스크립트
│   ├── build-local.sh         # 로컬 Docker 이미지 빌드
│   ├── deploy-local.sh        # K8s 배포
│   ├── port-forward.sh        # 포트포워딩
│   └── cleanup-local.sh       # 리소스 정리
├── k8s/                       # Kubernetes 매니페스트
│   ├── local/                 # 로컬 환경용
│   │   ├── kustomization.yaml
│   │   ├── configmap.yaml
│   │   ├── secrets-local.yaml
│   │   └── *.yaml
│   └── *.yaml                 # 프로덕션용
└── [service-name]/            # 각 마이크로서비스
    ├── build.gradle.kts
    └── src/main/
        ├── java/io/github/jiwontechinnovation/[service]/
        │   ├── *Application.java
        │   ├── config/
        │   ├── controller/
        │   ├── service/
        │   ├── repository/
        │   ├── entity/
        │   └── dto/
        └── resources/
            └── application.yml
```

## ⚙️ 환경 변수

### 데이터베이스 설정

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/jiwon-tech-innovation` | DB 연결 URL |
| `SPRING_DATASOURCE_USERNAME` | `shinseungmin` | DB 사용자명 |
| `SPRING_DATASOURCE_PASSWORD` | (empty) | DB 비밀번호 |

### Eureka 설정

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://localhost:8761/eureka/` | Eureka 서버 URL |

### 프로파일

| 변수명 | 값 | 설명 |
|--------|-----|------|
| `SPRING_PROFILES_ACTIVE` | `local` | 활성 프로파일 |

## 🛠️ 개발 가이드

### 새 서비스 추가

1. `settings.gradle.kts`에 모듈 추가:
   ```kotlin
   include(":new-service")
   ```

2. 서비스 디렉토리 생성 및 `build.gradle.kts` 작성

3. `application.yml`에 Eureka 클라이언트 설정 추가

4. K8s 매니페스트 작성 (`k8s/local/new-service.yaml`)

### Gradle 명령어

```bash
# 전체 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 특정 서비스 실행
./gradlew :auth-service:bootRun

# 의존성 확인
./gradlew dependencies

# 클린 빌드
./gradlew clean build
```

### Kubernetes 명령어

```bash
# 네임스페이스 Pod 상태 확인
kubectl get pods -n jiwon-tech

# 서비스 상태 확인
kubectl get svc -n jiwon-tech

# Pod 로그 확인
kubectl logs -f <pod-name> -n jiwon-tech

# Pod 재시작
kubectl rollout restart deployment/<service-name> -n jiwon-tech
```

## 📝 API 문서

Swagger UI를 통해 API 문서를 확인할 수 있습니다:

- **통합 Swagger UI**: http://localhost:8080/swagger-ui.html
  - auth-service, user-service, goal-service, analysis-service 선택 가능

각 서비스별 API 문서:
- Auth Service: `/auth-service/v3/api-docs`
- User Service: `/user-service/v3/api-docs`
- Goal Service: `/goal-service/v3/api-docs`
- Analysis Service: `/analysis-service/v3/api-docs`

## 🔐 인증

JWT 기반 인증을 사용합니다.

1. `/auth-service/api/auth/signup` - 회원가입
2. `/auth-service/api/auth/signin` - 로그인 (JWT 토큰 발급)
3. 이후 요청에 `Authorization: Bearer <token>` 헤더 포함

## 🐛 트러블슈팅

### Eureka에 서비스가 등록되지 않을 때

1. Discovery Service가 먼저 실행되었는지 확인
2. 각 서비스의 `application.yml`에서 Eureka URL 확인
3. 방화벽/네트워크 설정 확인

### Kubernetes Pod가 시작되지 않을 때

```bash
# Pod 상태 확인
kubectl describe pod <pod-name> -n jiwon-tech

# 이벤트 확인
kubectl get events -n jiwon-tech --sort-by='.lastTimestamp'
```

### 데이터베이스 연결 오류

- K8s 환경에서는 `host.docker.internal`을 사용하여 호스트 DB에 접근
- 로컬 실행 시에는 `localhost` 사용

