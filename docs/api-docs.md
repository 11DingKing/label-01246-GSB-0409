# 风力发电机故障诊断系统 - API 接口文档

> 版本：v1.0.0 | 基础路径：`http://localhost:8080/api` | 协议：HTTP/HTTPS

---

## 一、全局约定

### 1.1 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200 表示成功 |
| message | string | 提示信息 |
| data | any | 响应数据，无数据时为 null |

### 1.2 分页响应格式

```json
{
  "records": [],
  "total": 100,
  "current": 1,
  "size": 10,
  "pages": 10
}
```

### 1.3 错误码参考

| 错误码 | 含义 | 说明 |
|--------|------|------|
| 200 | 成功 | 请求处理成功 |
| 400 | 参数错误 | 请求参数校验失败 |
| 401 | 未认证 | 未登录或 Token 已过期 |
| 403 | 禁止访问 | CSRF 校验失败或无权限 |
| 500 | 服务器错误 | 系统内部异常 |

### 1.4 认证方式

除白名单接口外，所有接口需在请求头中携带 JWT Token：

```
Authorization: Bearer {token}
```

写操作（POST/PUT/DELETE）还需携带：

```
X-CSRF-Token: {csrf_token}
X-Requested-With: XMLHttpRequest
```

### 1.5 白名单接口（无需认证）

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/verify-email`
- `POST /api/auth/resend-verify-email`
- `GET /api/auth/csrf-token`

### 1.6 枚举值参考

**用户状态（status）**

| 值 | 含义 |
|----|------|
| 0 | 禁用 |
| 1 | 启用 |
| 2 | 待审核 |
| 3 | 待验证邮箱 |

**权限类型（permType）**

| 值 | 含义 |
|----|------|
| MENU | 菜单权限 |
| BUTTON | 按钮权限 |
| API | 接口权限 |

**数据范围（scopeType）**

| 值 | 含义 |
|----|------|
| 1 | 全部数据 |
| 2 | 本部门数据 |
| 3 | 本部门及下级数据 |
| 4 | 仅本人数据 |
| 5 | 自定义 |

---

## 二、认证模块（/api/auth）

### 2.1 用户登录

`POST /api/auth/login`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**请求示例：**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": "admin@windpower.com",
      "status": 1
    }
  }
}
```

**错误响应：**

| 场景 | code | message |
|------|------|---------|
| 用户名为空 | 400 | 用户名不能为空 |
| 用户不存在 | 500 | 用户名或密码错误 |
| 密码错误 | 500 | 用户名或密码错误 |
| 账户禁用 | 500 | 账户已被禁用 |
| 账户锁定 | 500 | 账户已被锁定，请联系管理员 |
| 待审核 | 500 | 账户正在审核中，请耐心等待 |
| 邮箱未验证 | 500 | 请先验证邮箱后再登录 |

---

### 2.2 用户注册

`POST /api/auth/register`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码（至少6位） |
| realName | string | 否 | 真实姓名 |
| email | string | 是 | 邮箱（用于验证） |

**请求示例：**

```json
{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "email": "zhangsan@example.com"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "注册成功，验证邮件已发送至您的邮箱，请查收并完成验证",
  "data": null
}
```

**说明：** 注册后用户状态为 3（待验证邮箱），系统自动发送验证邮件。默认分配"普通查看用户"角色。

---

### 2.3 邮箱验证

`GET /api/auth/verify-email`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | string | 是 | 验证 Token（邮件中的链接参数） |

**成功响应：**

```json
{
  "code": 200,
  "message": "邮箱验证成功！您的账户已进入审核流程，请等待管理员审核。",
  "data": null
}
```

**错误响应：**

| 场景 | message |
|------|---------|
| Token 为空 | 验证Token不能为空 |
| Token 无效 | 无效的验证链接 |
| Token 已使用 | 该验证链接已被使用 |
| Token 已过期 | 验证链接已过期，请重新发送验证邮件 |

---

### 2.4 重新发送验证邮件

`POST /api/auth/resend-verify-email`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 是 | 注册时使用的邮箱 |

**请求示例：**

```json
{
  "email": "zhangsan@example.com"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "验证邮件已重新发送，请查收",
  "data": null
}
```

---

### 2.5 获取 CSRF Token

`GET /api/auth/csrf-token`

**响应头：**

```
X-CSRF-Token: {csrf_token}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": "dGhpcyBpcyBhIGNzcmYgdG9rZW4..."
}
```

---

### 2.6 退出登录

`POST /api/auth/logout`

**说明：** JWT 无状态，服务端不做处理，客户端删除本地 Token 即可。

**成功响应：**

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

### 2.7 获取当前用户信息

`GET /api/auth/info`

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": "admin@windpower.com",
      "phone": null,
      "avatar": null,
      "deptId": 1,
      "emailVerified": 1,
      "status": 1,
      "accountLocked": 0,
      "lastLoginTime": "2025-01-01T10:00:00",
      "lastLoginIp": "127.0.0.1",
      "createdAt": "2025-01-01T00:00:00"
    },
    "roleIds": [1],
    "permissions": ["dashboard", "system", "system:user", "system:user:add", "..."],
    "menus": [
      {
        "id": 1,
        "parentId": 0,
        "permCode": "dashboard",
        "permName": "数据仪表盘",
        "permType": "MENU",
        "path": "/dashboard",
        "icon": "icon-dashboard",
        "component": "dashboard/index",
        "children": []
      }
    ],
    "dataScope": 1
  }
}
```

---

### 2.8 修改密码

`PUT /api/auth/password`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码（至少6位） |

**请求示例：**

```json
{
  "oldPassword": "admin123",
  "newPassword": "newpass123"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

### 2.9 重置密码（管理员操作）

`POST /api/auth/reset-password`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 目标用户 ID |

**请求示例：**

```json
{
  "userId": 2
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "密码已重置为默认密码: 123456",
  "data": null
}
```

---

## 三、用户管理模块（/api/user）

### 3.1 用户分页查询

`GET /api/user/page`

**查询参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | int | 否 | 1 | 当前页码 |
| size | int | 否 | 10 | 每页条数 |
| username | string | 否 | - | 用户名（模糊匹配） |
| realName | string | 否 | - | 真实姓名（模糊匹配） |
| status | int | 否 | - | 状态筛选 |

**说明：** 查询结果受当前用户数据范围权限限制。管理员可查看全部数据，其他角色根据数据范围配置过滤。

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "email": "admin@windpower.com",
        "phone": null,
        "avatar": null,
        "deptId": 1,
        "emailVerified": 1,
        "status": 1,
        "accountLocked": 0,
        "loginFailCount": 0,
        "lastLoginTime": "2025-01-01T10:00:00",
        "lastLoginIp": "127.0.0.1",
        "createdAt": "2025-01-01T00:00:00",
        "updatedAt": "2025-01-01T10:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

---

### 3.2 查询用户详情

`GET /api/user/{id}`

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 用户 ID |

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "email": "admin@windpower.com",
    "status": 1
  }
}
```

---

### 3.3 新增用户

`POST /api/user/`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| realName | string | 否 | 真实姓名 |
| email | string | 否 | 邮箱 |
| phone | string | 否 | 手机号 |

**请求示例：**

```json
{
  "username": "engineer01",
  "password": "123456",
  "realName": "工程师一号",
  "email": "eng01@windpower.com",
  "phone": "13800138000"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": null
}
```

---

### 3.4 编辑用户

`PUT /api/user/{id}`

**路径参数：** id (long) - 用户 ID

**请求体：** 同新增用户（username 和 password 不可修改）

---

### 3.5 删除用户

`DELETE /api/user/{id}`

**说明：** 逻辑删除，设置 deleted=1。

**成功响应：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 3.6 修改用户状态

`PUT /api/user/{id}/status`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 是 | 0=禁用, 1=启用 |

**请求示例：**

```json
{
  "status": 0
}
```

---

### 3.7 锁定/解锁用户

`PUT /api/user/{id}/lock`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| locked | int | 是 | 0=解锁, 1=锁定 |

**请求示例：**

```json
{
  "locked": 0
}
```

---

### 3.8 分配角色

`PUT /api/user/{id}/roles`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleIds | long[] | 是 | 角色 ID 列表 |

**请求示例：**

```json
{
  "roleIds": [1, 2]
}
```

---

### 3.9 查询用户角色

`GET /api/user/{id}/roles`

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [1, 4]
}
```

---

## 四、角色管理模块（/api/role）

### 4.1 角色分页查询

`GET /api/role/page`

**查询参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | int | 否 | 1 | 当前页码 |
| size | int | 否 | 10 | 每页条数 |
| roleName | string | 否 | - | 角色名称（模糊匹配） |
| status | int | 否 | - | 状态筛选 |

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "roleCode": "ADMIN",
        "roleName": "系统管理员",
        "description": "拥有系统全部权限",
        "dataScope": 1,
        "status": 1,
        "sortOrder": 1,
        "createdAt": "2025-01-01T00:00:00"
      }
    ],
    "total": 4,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

---

### 4.2 角色列表（全量）

`GET /api/role/list`

**说明：** 返回所有启用的角色，用于下拉选择。

---

### 4.3 查询角色详情

`GET /api/role/{id}`

**说明：** 返回角色信息及其关联的权限 ID 列表。

---

### 4.4 新增角色

`POST /api/role/`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleCode | string | 是 | 角色编码（唯一） |
| roleName | string | 是 | 角色名称 |
| description | string | 否 | 角色描述 |
| status | int | 否 | 状态，默认 1 |
| sortOrder | int | 否 | 排序，默认 0 |

**请求示例：**

```json
{
  "roleCode": "OPERATOR",
  "roleName": "操作员",
  "description": "负责日常操作",
  "status": 1,
  "sortOrder": 5
}
```

---

### 4.5 编辑角色

`PUT /api/role/{id}`

**请求体：** 同新增角色。

---

### 4.6 删除角色

`DELETE /api/role/{id}`

---

### 4.7 分配权限

`PUT /api/role/{id}/permissions`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| permIds | long[] | 是 | 权限 ID 列表 |

**请求示例：**

```json
{
  "permIds": [1, 2, 3, 10, 11]
}
```

---

### 4.8 查询角色权限

`GET /api/role/{id}/permissions`

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [1, 2, 3, 10, 11, 12, 13]
}
```

---

### 4.9 配置数据范围

`PUT /api/role/{id}/data-scope`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| scopeType | int | 是 | 1=全部 2=本部门 3=本部门及下级 4=仅本人 5=自定义 |

**请求示例：**

```json
{
  "scopeType": 2
}
```

---

### 4.10 查询数据范围

`GET /api/role/{id}/data-scope`

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 2
}
```

---

## 五、权限管理模块（/api/permission）

### 5.1 权限树查询

`GET /api/permission/tree`

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "permCode": "dashboard",
      "permName": "数据仪表盘",
      "permType": "MENU",
      "path": "/dashboard",
      "icon": "icon-dashboard",
      "component": "dashboard/index",
      "sortOrder": 1,
      "status": 1,
      "children": []
    },
    {
      "id": 2,
      "parentId": 0,
      "permCode": "system",
      "permName": "系统管理",
      "permType": "MENU",
      "path": "/system",
      "icon": "icon-settings",
      "children": [
        {
          "id": 3,
          "parentId": 2,
          "permCode": "system:user",
          "permName": "用户管理",
          "permType": "MENU",
          "path": "/system/user",
          "children": [
            {
              "id": 10,
              "parentId": 3,
              "permCode": "system:user:add",
              "permName": "新增用户",
              "permType": "BUTTON",
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

---

### 5.2 新增权限

`POST /api/permission/`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | long | 否 | 父权限 ID，0 为顶级，默认 0 |
| permCode | string | 是 | 权限编码（唯一） |
| permName | string | 是 | 权限名称 |
| permType | string | 是 | 类型：MENU / BUTTON / API |
| path | string | 否 | 路由路径（MENU 类型需要） |
| icon | string | 否 | 图标 |
| component | string | 否 | 前端组件路径 |
| sortOrder | int | 否 | 排序，默认 0 |
| status | int | 否 | 状态，默认 1 |

---

### 5.3 编辑权限

`PUT /api/permission/{id}`

**请求体：** 同新增权限。

---

### 5.4 删除权限

`DELETE /api/permission/{id}`

**说明：** 如果存在子权限，需先删除子权限。

---

## 六、部门管理模块（/api/dept）

### 6.1 部门树查询

`GET /api/dept/tree`

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "deptName": "总公司",
      "sortOrder": 1,
      "status": 1,
      "children": [
        {
          "id": 2,
          "parentId": 1,
          "deptName": "运维部",
          "sortOrder": 1,
          "children": []
        },
        {
          "id": 3,
          "parentId": 1,
          "deptName": "数据分析部",
          "sortOrder": 2,
          "children": []
        }
      ]
    }
  ]
}
```

---

### 6.2 部门列表（平铺）

`GET /api/dept/list`

---

### 6.3 查询部门详情

`GET /api/dept/{id}`

---

### 6.4 新增部门

`POST /api/dept/`

**请求体：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | long | 否 | 父部门 ID，0 为顶级 |
| deptName | string | 是 | 部门名称 |
| sortOrder | int | 否 | 排序 |

---

### 6.5 编辑部门

`PUT /api/dept/{id}`

---

### 6.6 删除部门

`DELETE /api/dept/{id}`

**说明：** 存在子部门时无法删除。

---

## 七、日志管理模块（/api/log）

### 7.1 操作日志分页查询

`GET /api/log/operation/page`

**查询参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | int | 否 | 1 | 当前页码 |
| size | int | 否 | 10 | 每页条数 |
| username | string | 否 | - | 操作用户名 |
| module | string | 否 | - | 操作模块 |

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "username": "admin",
        "module": "用户管理",
        "operation": "新增用户",
        "method": "POST",
        "url": "/api/user/",
        "params": "{...}",
        "result": "{...}",
        "ip": "127.0.0.1",
        "duration": 45,
        "status": 1,
        "errorMsg": null,
        "createdAt": "2025-01-01T10:30:00"
      }
    ],
    "total": 50,
    "current": 1,
    "size": 10,
    "pages": 5
  }
}
```

---

### 7.2 登录日志分页查询

`GET /api/log/login/page`

**查询参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | int | 否 | 1 | 当前页码 |
| size | int | 否 | 10 | 每页条数 |
| username | string | 否 | - | 用户名 |
| status | int | 否 | - | 状态：0=失败 1=成功 |

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "username": "admin",
        "ip": "127.0.0.1",
        "userAgent": "Mozilla/5.0...",
        "loginType": 1,
        "status": 1,
        "message": "登录成功",
        "createdAt": "2025-01-01T10:00:00"
      }
    ],
    "total": 20,
    "current": 1,
    "size": 10,
    "pages": 2
  }
}
```

---

## 八、健康检查

### 8.1 服务状态

`GET /`

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "service": "wind-power-diag",
    "status": "UP",
    "version": "1.0.0"
  }
}
```
