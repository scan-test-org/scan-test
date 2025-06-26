# API 开放平台

基于 Spring Boot 的开放平台管理系统。

## 项目结构

```
portal-management/
├── portal-dal/           # 数据访问层
│   ├── entity/           # 数据库实体
│   ├── repository/       # 数据访问接口
│   └── support/          # 辅助数据结构
│
├── portal-server/        # 业务逻辑层
|   ├── auth/             # 认证鉴权
│   ├── controller/       # REST接口
│   ├── service/          # 业务服务
│   ├── dto/              # 数据传输对象
│   └── config/           # 业务配置
│
└── portal-bootstrap/     # 应用启动模块
    ├── Application.java  # 主启动类
    └── config/           # 全局配置
```

## 模块依赖
项目采用分层架构，依赖关系如下：
```
portal-bootstrap -> portal-server -> portal-dal
```
- portal-dal: 数据访问基础模块，提供数据实体和访问接口
- portal-server: 业务逻辑模块，依赖 portal-dal
- portal-bootstrap: 应用程序入口，依赖 portal-server

## 技术栈
- Java 1.8
- Spring Boot 2.7.18
- Spring Data JPA
- MySQL 8.0.33
- Maven

## 启动准备
在 portal-dal 的 application.yaml 配置文件中，修改 MySQL 数据库地址、用户名和密码

## 注意事项
1. 所有依赖的版本统一在父 pom.xml 中管理
2. 尽量不引入额外的 xxUtil（比如 StringUtil），推荐使用开源工具 Hutool
3. Test 用例可在 portal-bootstrap 模块中编写
4. 推荐 Bean 使用全参构造方法初始化，非 Test 场景不使用 AutoWired 注解
5. 推荐使用 lombok 注解简化代码