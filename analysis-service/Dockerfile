FROM amazoncorretto:21-al2023-headless

WORKDIR /app

# 2. 패키지 업데이트 (Amazon Linux 2023용)
RUN dnf update -y && dnf clean all

# 3. JAR 복사 및 실행 (기존과 동일)
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]