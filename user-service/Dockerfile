# 1. 실행만 할 거니까 JRE 버전으로 충분합니다! (이미지 용량 다이어트!)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 2. 젠킨스가 이미 구워놓은 JAR 파일만 쏙 가져옵니다!
# (경로: 현재 폴더 안의 build/libs/ 에서 가져오는 겁니다)
COPY build/libs/*.jar app.jar

# 3. 바로 실행
ENTRYPOINT ["java", "-jar", "app.jar"]