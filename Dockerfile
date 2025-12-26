# 멀티스테이지 빌드 - 빌드 단계
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle 관련 파일 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# 서브모듈 build.gradle.kts 복사
COPY auth-service/build.gradle.kts auth-service/
COPY gateway-service/build.gradle.kts gateway-service/
COPY discovery-service/build.gradle.kts discovery-service/
COPY user-service/build.gradle.kts user-service/
COPY goal-service/build.gradle.kts goal-service/
COPY analysis-service/build.gradle.kts analysis-service/
COPY common-lib/build.gradle.kts common-lib/

# 의존성 다운로드 (캐시 레이어)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY . .

# 빌드할 모듈 지정 (예: auth-service, gateway-service 등)
ARG MODULE

# 해당 모듈만 빌드
RUN ./gradlew :${MODULE}:bootJar --no-daemon -x test

# 실행 단계
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌드할 모듈 지정
ARG MODULE

# 빌드된 JAR 복사
COPY --from=builder /app/${MODULE}/build/libs/*.jar app.jar

# 보안: non-root 사용자로 실행
RUN addgroup -g 1000 spring && adduser -u 1000 -G spring -s /bin/sh -D spring
USER spring:spring

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


