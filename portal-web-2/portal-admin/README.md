# Portal Admin

基于 Vite + React + Redux + React Router 的门户管理系统前端。

## 技术栈

- **构建工具**: Vite
- **前端框架**: React 19
- **状态管理**: Redux Toolkit
- **路由**: React Router DOM
- **UI组件**: Radix UI + Tailwind CSS
- **HTTP客户端**: Axios
- **表单处理**: React Hook Form + Zod

## 开发环境设置

1. 安装依赖：
```bash
npm install
```

2. 配置环境变量：
```bash
cp env.example .env
```

3. 启动开发服务器：
```bash
npm run dev
```

4. 构建生产版本：
```bash
npm run build
```

## 项目结构

```
src/
├── components/     # 可复用组件
├── pages/         # 页面组件
├── store/         # Redux store配置
│   └── slices/    # Redux slices
├── routes/        # 路由配置
├── lib/           # 工具函数和API配置
└── hooks/         # 自定义hooks
```

## 环境变量

- `VITE_API_BASE_URL`: API基础URL
- `VITE_APP_TITLE`: 应用标题

## 开发指南

- 使用 `@/` 路径别名指向 `src/` 目录
- 组件使用 Tailwind CSS 进行样式设计
- 状态管理使用 Redux Toolkit
- API调用使用配置好的 axios 实例
