# React + TypeScript + Vite



## 部署方式

1. 本地构建
```bash
npm run build
```

2. 构建本地镜像
```bash
docker build -t /daofeng/api-portal-admin:latest .
```

3. 推送镜像到ACR
```bash
docker push /daofeng/api-portal-admin:latest
```

4. ACK 对应的无状态重新部署

