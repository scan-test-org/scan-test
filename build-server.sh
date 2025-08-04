#!/bin/bash

VERSION="1.0.0"
REGISTRY="registry.cn-shanghai.aliyuncs.com/daofeng"
IMAGE_NAME="api-portal-server"

echo "=== Starting build process for $IMAGE_NAME ==="

# Maven 构建
echo "Building with Maven..."
mvn clean package -DskipTests

# 检查 Maven 构建是否成功
if [ $? -ne 0 ]; then
    echo "Maven build failed"
    exit 1
fi
echo "Maven build successful"

echo "Changing to portal-bootstrap directory..."
cd portal-bootstrap || exit 1

# 构建并推送 Docker 镜像
echo "Building and pushing Docker image..."
docker buildx build \
    --platform linux/amd64 \
    -t $REGISTRY/$IMAGE_NAME:$VERSION \
    --push .

# 检查 Docker 构建是否成功
if [ $? -ne 0 ]; then
    echo "Docker build and push failed"
    exit 1
fi

echo "=== Build and push completed successfully ==="
echo "Image: $REGISTRY/$IMAGE_NAME:$VERSION"
