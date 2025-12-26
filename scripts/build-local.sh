#!/bin/bash

# 로컬 Docker 이미지 빌드 스크립트 (최적화 버전)
set -e

echo "🔨 Building all services for local Kubernetes..."

SERVICES=("discovery-service" "gateway-service" "auth-service" "user-service" "goal-service" "analysis-service")

# Step 1: 호스트에서 전체 빌드 (Gradle 캐시 활용)
echo ""
echo "📦 Step 1: Building all JARs locally (uses Gradle cache)..."
./gradlew clean bootJar -x test --parallel

# Step 2: 각 서비스별로 가벼운 Docker 이미지 생성
echo ""
echo "🐳 Step 2: Building Docker images..."

for SERVICE in "${SERVICES[@]}"; do
    echo ""
    echo "📦 Building $SERVICE image..."
    
    # JAR 파일 찾기
    JAR_FILE=$(find ${SERVICE}/build/libs -name "*.jar" ! -name "*-plain.jar" 2>/dev/null | head -1)
    
    if [ -z "$JAR_FILE" ]; then
        echo "❌ JAR not found for $SERVICE"
        exit 1
    fi
    
    # 경량 Dockerfile로 이미지 빌드
    docker build -f Dockerfile.local --build-arg JAR_FILE=$JAR_FILE -t $SERVICE:local .
done

echo ""
echo "✅ All images built successfully!"
echo ""
echo "Built images:"
docker images | grep ":local"


