#!/bin/bash

# 设置错误时退出
set -e

# 安装依赖
npm i

# 构建
npm run build

# 构建镜像
docker buildx build --platform linux/amd64 -t registry.cn-shanghai.aliyuncs.com/daofeng/api-portal-frontend:latest .

# 推送镜像
docker push registry.cn-shanghai.aliyuncs.com/daofeng/api-portal-frontend:latest