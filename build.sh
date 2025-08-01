#!/bin/bash

# 设置错误时退出
set -e

# 安装依赖
npm i

# 构建
npm run build

# 构建镜像
docker build -t /daofeng/api-portal-admin:latest .

# 推送镜像
docker push /daofeng/api-portal-admin:latest