#!/bin/bash

# 로컬 Kubernetes 리소스 정리
echo "🧹 Cleaning up local Kubernetes resources..."

# Gateway Service 포트포워딩 프로세스 종료
echo ""
echo "🔌 Stopping Gateway Service port forwarding..."
GATEWAY_PF_PID=$(lsof -ti:8080 2>/dev/null || true)
if [ ! -z "$GATEWAY_PF_PID" ]; then
    echo "   포트포워딩 프로세스 종료 중 (PID: $GATEWAY_PF_PID)..."
    kill $GATEWAY_PF_PID 2>/dev/null || true
    sleep 1
    echo "   ✅ 포트포워딩 프로세스 종료 완료"
else
    echo "   ℹ️  실행 중인 포트포워딩 프로세스가 없습니다"
fi

# Kubernetes 리소스 삭제
echo ""
echo "🗑️  Deleting Kubernetes resources..."
kubectl delete -k k8s/local/ --ignore-not-found

echo ""
echo "✅ Cleanup complete!"


