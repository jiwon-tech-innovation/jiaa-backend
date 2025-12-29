#!/bin/bash

# Gateway 서비스 포트포워딩
echo "🌐 Port forwarding Gateway Service to localhost:8080..."
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop"

kubectl port-forward svc/gateway-service 8080:8080 -n jiwon-tech


