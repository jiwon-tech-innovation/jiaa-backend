# 레거시 Dockerfile (사용 안 함)

이 파일들은 현재 사용되지 않으며, 각 서비스 폴더의 Dockerfile을 사용합니다.

## 파일 목록

- `Dockerfile` - 멀티스테이지 빌드 (MODULE 빌드 인자 사용)
- `Dockerfile.local` - 로컬 개발용 경량 Dockerfile

## 현재 사용 방식

각 서비스 폴더의 Dockerfile을 사용:
- `gateway-service/Dockerfile`
- `auth-service/Dockerfile`
- `user-service/Dockerfile`
- 등등...

## 제거 가능

이 파일들은 제거해도 됩니다. 필요시 Git 히스토리에서 복구 가능합니다.

