# 管理员相关接口文档

> 本文档描述了管理员初始化、登录、登出、修改密码等接口的请求参数、返回结构及异常码说明。
> 
> @author zxd

---

## 1. 管理员初始化检测

**GET /api/admin/need-init?portalId=xxx**

- 说明：检测指定 portalId 下是否需要初始化管理员
- 请求参数：
  - portalId (string, 必填)
- 返回：
  - { "code": "SUCCESS", "message": "操作成功", "data": true/false }
- 异常：无

---

## 2. 管理员初始化

**POST /api/admin/init**

- 说明：初始化指定 portalId 下的第一个管理员，仅允许首次调用
- 请求体：
  - portalId (string, 必填)
  - username (string, 必填)
  - password (string, 必填)
- 返回：
  - { "code": "SUCCESS", "message": "初始化成功", "data": null }
- 异常：
  - ADMIN_ALREADY_INITIALIZED
  - ADMIN_USERNAME_EXISTS

---

## 3. 管理员登录

**POST /api/admin/login**

- 说明：管理员登录，需带 portalId
- 请求体：
  - portalId (string, 必填)
  - username (string, 必填)
  - password (string, 必填)
- 返回：
  - { "code": "SUCCESS", "message": "操作成功", "data": { token, userId, username, userType } }
- 异常：
  - ADMIN_NOT_FOUND
  - ADMIN_PASSWORD_INCORRECT
  - ADMIN_INIT_REQUIRED

---

## 4. 管理员登出

**POST /api/admin/logout**

- 说明：管理员登出，需带 portalId 和 Authorization 头
- 请求参数：
  - portalId (string, 必填)
  - Authorization (header, Bearer token, 必填)
- 返回：
  - { "code": "SUCCESS", "message": "操作成功", "data": null }
- 异常：无

---

## 5. 管理员修改密码

**POST /api/admin/change-password**

- 说明：管理员修改密码，需带 portalId、adminId、oldPassword、newPassword
- 请求参数：
  - portalId (string, 必填)
  - adminId (string, 必填)
  - oldPassword (string, 必填)
  - newPassword (string, 必填)
- 返回：
  - { "code": "SUCCESS", "message": "修改密码成功", "data": null }
- 异常：
  - ADMIN_NOT_FOUND
  - ADMIN_PASSWORD_INCORRECT

---

## 6. 异常码说明

| code                      | http status | message                        |
|---------------------------|-------------|--------------------------------|
| ADMIN_ALREADY_INITIALIZED | 409         | 该portal已初始化管理员，禁止重复初始化 |
| ADMIN_NOT_FOUND           | 404         | 管理员[%s]不存在                |
| ADMIN_PASSWORD_INCORRECT  | 401         | 原密码错误                      |
| ADMIN_USERNAME_EXISTS     | 409         | 用户名[%s]已存在                 |
| ADMIN_INIT_REQUIRED       | 403         | 请先初始化管理员账户             |
| INVALID_PARAMETER         | 400         | 请求参数[%s]的值无效或格式错误    |
| AUTH_INVALID              | 401         | 用户认证失败，请检查账号密码      |
| RESOURCE_EXIST            | 409         | 资源[%s:%s]已存在，请勿重复创建   |
| RESOURCE_NOT_FOUND        | 404         | 资源[%s:%s]不存在                |
| SERVER_ERROR              | 500         | 服务器内部错误，请稍后重试        |

---

> 所有接口返回结构均为：
> { "code": "...", "message": "...", "data": ... } 