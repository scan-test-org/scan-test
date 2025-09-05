# AI 开放平台 Helm 部署指南

## 📋 项目说明

Helm 会部署三个应用，分别为：
- `api-portal-server`：AI 开发平台的后端服务，运行在 8080 端口；
- `api-portal-admin`：AI 开放平台管理后台，管理员通过此界面配置 Portal；
- `api-portal-frontend`：AI 开放平台的前台服务，用户通过此界面浏览和使用 API。

服务类型说明：

`api-portal-admin` 和 `api-portal-frontend` 默认为 LoadBalancer 类型服务，适用于云环境（阿里云 ACK、AWS EKS 等）。如果您的环境不支持 LoadBalancer（如本地 minikube、自建集群），可以使用 NodePort 或端口转发方式访问。后台配置好 Portal 后，将域名解析到 frontend 服务的访问地址，用户就可以通过域名访问前台站点。

**镜像说明：**

默认使用公开镜像仓库的镜像：
- `api-portal-server`: `opensource-registry.cn-hangzhou.cr.aliyuncs.com/higress-group/api-portal-server:1.0.0`
- `api-portal-admin`: `opensource-registry.cn-hangzhou.cr.aliyuncs.com/higress-group/api-portal-admin:1.0.0`
- `api-portal-frontend`: `opensource-registry.cn-hangzhou.cr.aliyuncs.com/higress-group/api-portal-frontend:1.0.0`

如需使用自定义镜像，可修改 `values.yaml` 中的镜像配置，或通过 `--set` 参数指定：
```bash
helm install api-portal . --namespace api-portal --create-namespace \
  --set api_portal_server.image.repository=your-custom-server-image \
  --set api_portal_admin.image.repository=your-custom-admin-image \
  --set api_portal_frontend.image.repository=your-custom-frontend-image
```

## ⚠️ 重要说明

**本项目使用外置 MySQL 数据库，部署前必须先准备数据库并配置连接信息！**

## 🛠️ 部署前准备

### 数据库要求

准备一个 MySQL 数据库实例，建议配置：
- **数据库名**: 如`portal_db`
- **用户名**: 如`portal_user`  
- **密码**: 自定义密码

## 🚀 部署步骤

### 1. 配置数据库连接

编辑 `templates/api-portal-server-cm.yaml` 文件，替换数据库配置：

```yaml
data:
  SERVER_PORT: "8080"
  # External Database Configuration - 请替换为你的数据库信息
  DB_HOST: "your-mysql-host"                            # 数据库地址
  DB_PORT: "your-db-port"                               # 数据库端口
  DB_NAME: "portal_db"                                  # 数据库名
  DB_USERNAME: "portal_user"                            # 数据库用户名
  DB_PASSWORD: "your_password"                          # 数据库密码
```

### 2. 部署应用

```bash
# 进入 Helm 目录
cd deploy/helm

# 部署到 Kubernetes
helm install api-portal . --namespace api-portal --create-namespace
```

### 3. 验证部署

```bash
# 查看 Pod 状态（等待所有 Pod 变为 Running）
kubectl get pods -n api-portal

# 获取访问地址
kubectl get svc -n api-portal
```

## 📝 服务说明

| 服务 | 类型 | 端口          | 说明         |
|------|------|-------------|------------|
| **api-portal-frontend** | LoadBalancer | 80 → 8000   | 开发者门户，前端服务 |
| **api-portal-admin** | LoadBalancer | 80 → 8000   | 管理后台，前端服务  |
| **api-portal-server** | ClusterIP | 8080 → 8080 | 后端服务       |

## 🌐 访问应用

### 方式一：LoadBalancer（云环境推荐）

```bash
# 获取外部 IP 地址
kubectl get svc -n api-portal

# 等待 EXTERNAL-IP 分配完成，然后访问：
# Frontend: http://<frontend-external-ip>
# Admin: http://<admin-external-ip>
```

### 方式二：NodePort（本地环境推荐）

如果 LoadBalancer 不可用，可以修改服务类型为 NodePort：

```bash
# 修改服务类型为 NodePort
kubectl patch svc api-portal-frontend -n api-portal -p '{"spec":{"type":"NodePort"}}'
kubectl patch svc api-portal-admin -n api-portal -p '{"spec":{"type":"NodePort"}}'

# 获取 NodePort 端口
kubectl get svc -n api-portal

# 通过节点 IP + NodePort 访问：
# Frontend: http://<node-ip>:<nodeport>
# Admin: http://<node-ip>:<nodeport>
```