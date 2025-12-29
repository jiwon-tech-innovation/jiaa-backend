FROM amazoncorretto:21-al2023-headless

WORKDIR /app

# 패키지 업데이트 (Amazon Linux 2023용)
RUN dnf update -y && dnf clean all

# JAR 복사 및 실행
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

