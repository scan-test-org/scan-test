#!/bin/bash
set -e

# 配置
REPOSITORY=""
ALIYUN_USER="mse_dev_chengpu_testcloud_com"
ALIYUN_PASSWORD=""
NAMESPACE="daofeng"
VERSION="1.0.0"

echo "开始构建和推送镜像..."

# 构建并推送 api-portal-admin
echo "处理 api-portal-admin..."
cd api-portal-admin
# npm run build
docker buildx build -t $REPOSITORY/$NAMESPACE/api-portal-admin:$VERSION --platform linux/amd64 -o type=docker .
cd ..

# 构建并推送 api-portal-frontend
echo "处理 api-portal-frontend..."
cd api-portal-frontend
# npm run build
docker buildx build -t $REPOSITORY/$NAMESPACE/api-portal-frontend:$VERSION --platform linux/amd64 -o type=docker .
cd ..

# 登录并推送
echo "登录镜像仓库..."
echo $ALIYUN_PASSWORD | docker login $REPOSITORY --username $ALIYUN_USER --password-stdin

echo "推送镜像..."
docker push $REPOSITORY/$NAMESPACE/api-portal-admin:$VERSION
docker push $REPOSITORY/$NAMESPACE/api-portal-frontend:$VERSION

echo "完成！"
