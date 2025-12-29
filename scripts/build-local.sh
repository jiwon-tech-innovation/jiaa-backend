#!/bin/bash

# 로컬 Docker 이미지 빌드 스크립트 (각 서비스 폴더의 Dockerfile 사용)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🔨 Building all services for local Kubernetes..."

SERVICES=("discovery-service" "gateway-service" "auth-service" "user-service" "goal-service" "analysis-service")

# Step 1: 호스트에서 전체 빌드 (Gradle 캐시 활용)
echo ""
echo "📦 Step 1: Building all JARs locally (uses Gradle cache)..."
cd "$ROOT_DIR"

# Gradle daemon lock 문제 해결 시도
echo "   🔧 Stopping any running Gradle daemons..."
./gradlew --stop 2>/dev/null || true

# --no-daemon 옵션으로 lock 문제 우회 (디스크 공간 부족 시에도 안전)
echo "   🔨 Building JARs (no daemon mode to avoid lock issues)..."
./gradlew clean bootJar -x test --no-daemon --parallel

# Step 2: 각 서비스별로 Dockerfile을 사용하여 이미지 생성
echo ""
echo "🐳 Step 2: Building Docker images using service-specific Dockerfiles..."

for SERVICE in "${SERVICES[@]}"; do
    echo ""
    echo "📦 Building $SERVICE image..."
    
    SERVICE_DIR="$ROOT_DIR/$SERVICE"
    
    # 서비스 디렉토리 확인
    if [ ! -d "$SERVICE_DIR" ]; then
        echo "❌ Service directory not found: $SERVICE_DIR"
        exit 1
    fi
    
    # Dockerfile 확인
    if [ ! -f "$SERVICE_DIR/Dockerfile" ]; then
        echo "❌ Dockerfile not found: $SERVICE_DIR/Dockerfile"
        exit 1
    fi
    
    # JAR 파일 확인
    JAR_FILE=$(find "$SERVICE_DIR/build/libs" -name "*.jar" ! -name "*-plain.jar" 2>/dev/null | head -1)
    
    if [ -z "$JAR_FILE" ]; then
        echo "❌ JAR not found for $SERVICE. Building..."
        cd "$ROOT_DIR"
        ./gradlew :${SERVICE}:bootJar -x test --no-daemon
    fi
    
    # 각 서비스 폴더의 Dockerfile 사용 (빌드 컨텍스트는 서비스 폴더)
    cd "$SERVICE_DIR"
    
    # .dockerignore가 없으면 생성
    if [ ! -f "$SERVICE_DIR/.dockerignore" ]; then
        echo "   ⚠️  .dockerignore not found, creating one..."
        cat > "$SERVICE_DIR/.dockerignore" << 'EOF'
# Git
.git
.gitignore

# IDE
.idea
*.iml
.vscode

# Build outputs (JAR만 필요)
**/build
!build/libs/*.jar

# Gradle
.gradle
gradlew
gradlew.bat
gradle/

# Source code (JAR만 필요하므로)
src/
!build/libs/

# Test
**/test

# Logs
*.log

# Docker
Dockerfile*
docker-compose*

# K8s
k8s/

# Misc
*.md
LICENSE
Untitled
EOF
    fi
    
    docker build -f Dockerfile -t ${SERVICE}:local .
    echo "   ✅ Built: ${SERVICE}:local"
done

cd "$ROOT_DIR"
echo ""
echo "✅ All images built successfully!"
echo ""
echo "Built images:"
docker images | grep ":local"


