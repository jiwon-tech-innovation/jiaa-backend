#!/bin/bash

# 로컬 Kubernetes 배포 스크립트
set -e

echo "🚀 Deploying to local Kubernetes (Docker Desktop)..."

# 컨텍스트 확인
CONTEXT=$(kubectl config current-context)
if [[ "$CONTEXT" != "docker-desktop" ]]; then
    echo "⚠️  현재 컨텍스트: $CONTEXT"
    echo "Docker Desktop 컨텍스트로 변경하시겠습니까? (y/n)"
    read -r answer
    if [[ "$answer" == "y" ]]; then
        kubectl config use-context docker-desktop
    else
        echo "취소됨"
        exit 1
    fi
fi

# Kustomize로 배포
echo ""
echo "📦 Applying Kustomize..."
kubectl apply -k k8s/local/

# Discovery Service 대기
echo ""
echo "⏳ Waiting for Discovery Service to be ready..."
kubectl wait --for=condition=ready pod -l app=discovery-service -n jiwon-tech --timeout=180s

# 모든 서비스 대기
echo ""
echo "⏳ Waiting for all services to be ready..."
kubectl wait --for=condition=ready pod --all -n jiwon-tech --timeout=300s

echo ""
echo "✅ Deployment complete!"
echo ""
echo "📋 Pod status:"
kubectl get pods -n jiwon-tech

echo ""
echo "🌐 Service status:"
kubectl get svc -n jiwon-tech

# Gateway Service 포트포워딩 (기존 포트포워딩이 있으면 종료 후 재시작)
echo ""
echo "🔌 Setting up Gateway Service port forwarding..."

# 기존 포트포워딩 프로세스 확인 및 종료
GATEWAY_PF_PID=$(lsof -ti:8080 2>/dev/null || true)
if [ ! -z "$GATEWAY_PF_PID" ]; then
    echo "   기존 포트포워딩 프로세스 종료 중 (PID: $GATEWAY_PF_PID)..."
    kill $GATEWAY_PF_PID 2>/dev/null || true
    sleep 1
fi

# Gateway Service가 준비될 때까지 대기
echo "   Gateway Service 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=gateway-service -n jiwon-tech --timeout=60s || true

# 포트포워딩 백그라운드 실행
echo "   Gateway Service 포트포워딩 시작 (localhost:8080)..."
kubectl port-forward svc/gateway-service 8080:8080 -n jiwon-tech > /dev/null 2>&1 &
GATEWAY_PF_PID=$!
sleep 2

# 포트포워딩이 정상적으로 실행 중인지 확인
if ps -p $GATEWAY_PF_PID > /dev/null; then
    echo "   ✅ Gateway Service 포트포워딩 완료 (PID: $GATEWAY_PF_PID)"
    echo "   🌐 Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "   💡 포트포워딩을 중지하려면: kill $GATEWAY_PF_PID"
else
    echo "   ⚠️  포트포워딩 시작 실패 (이미 다른 프로세스가 8080 포트를 사용 중일 수 있습니다)"
fi

echo ""
echo "✅ 모든 설정 완료!"

