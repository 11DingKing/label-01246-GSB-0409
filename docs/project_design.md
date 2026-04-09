# 风力发电机故障诊断系统 - 项目设计文档

## 1. 系统架构

```mermaid
flowchart TD
    subgraph Frontend["前端 (Vue 3 + Arco Design Pro)"]
        A[登录页面] --> B[主布局]
        B --> C[数据仪表盘]
        B --> D[用户管理]
        B --> E[角色管理]
        B --> F[权限管理]
        B --> G[操作日志]
    end

    subgraph Backend["后端 (Solon 3.7.3 + JDK 21)"]
        H[AuthController] --> L[AuthService]
        I[UserController] --> M[UserService]
        J[RoleController] --> N[RoleService]
        K[LogController] --> O[LogService]
        L --> P[JwtUtil]
        L --> Q[PasswordEncoder]
    end

    subgraph Database["数据库 (MySQL 8.0)"]
        R[(sys_user)]
        S[(sys_role)]
        T[(sys_permission)]
        U[(sys_user_role)]
        V[(sys_role_permission)]
        W[(sys_operation_log)]
        X[(sys_login_log)]
    end

    Frontend -->|HTTP/REST| Backend
    Backend -->|MyBatis-Plus| Database
```

## 2. ER 图

```mermaid
erDiagram
    SYS_USER {
        bigint id PK
        varchar username UK
        varchar password
        varchar real_name
        varchar email
        varchar phone
        tinyint status
        tinyint account_locked
        int login_fail_count
        datetime last_login_time
        datetime created_at
        datetime updated_at
    }

    SYS_ROLE {
        bigint id PK
        varchar role_code UK
        varchar role_name
        varchar description
        tinyint status
        datetime created_at
        datetime updated_at
    }

    SYS_PERMISSION {
        bigint id PK
        bigint parent_id FK
        varchar perm_code UK
        varchar perm_name
        varchar perm_type
        varchar path
        varchar icon
        int sort_order
        tinyint status
        datetime created_at
    }

    SYS_USER_ROLE {
        bigint id PK
        bigint user_id FK
        bigint role_id FK
    }

    SYS_ROLE_PERMISSION {
        bigint id PK
        bigint role_id FK
        bigint perm_id FK
    }

    SYS_OPERATION_LOG {
        bigint id PK
        bigint user_id FK
        varchar username
        varchar module
        varchar operation
        varchar method
        varchar params
        varchar ip
        int duration
        tinyint status
        varchar error_msg
        datetime created_at
    }

    SYS_LOGIN_LOG {
        bigint id PK
        bigint user_id FK
        varchar username
        varchar ip
        varchar user_agent
        tinyint login_type
        tinyint status
        varchar message
        datetime created_at
    }

    SYS_USER ||--o{ SYS_USER_ROLE : "拥有"
    SYS_ROLE ||--o{ SYS_USER_ROLE : "分配给"
    SYS_ROLE ||--o{ SYS_ROLE_PERMISSION : "拥有"
    SYS_PERMISSION ||--o{ SYS_ROLE_PERMISSION : "分配给"
    SYS_PERMISSION ||--o{ SYS_PERMISSION : "父子关系"
    SYS_USER ||--o{ SYS_OPERATION_LOG : "产生"
    SYS_USER ||--o{ SYS_LOGIN_LOG : "产生"
```

## 3. 接口清单

### 3.1 AuthController (`/api/auth`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /login | 用户登录 |
| POST | /register | 用户注册 |
| POST | /logout | 用户登出 |
| GET  | /info | 获取当前用户信息 |
| PUT  | /password | 修改密码 |
| POST | /reset-password | 重置密码 |

### 3.2 UserController (`/api/user`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /page | 分页查询用户 |
| GET  | /{id} | 查询用户详情 |
| POST | / | 新增用户 |
| PUT  | /{id} | 修改用户 |
| DELETE | /{id} | 删除用户 |
| PUT  | /{id}/status | 启用/禁用用户 |
| PUT  | /{id}/lock | 锁定/解锁用户 |
| PUT  | /{id}/roles | 分配角色 |

### 3.3 RoleController (`/api/role`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /page | 分页查询角色 |
| GET  | /list | 查询全部角色 |
| GET  | /{id} | 查询角色详情 |
| POST | / | 新增角色 |
| PUT  | /{id} | 修改角色 |
| DELETE | /{id} | 删除角色 |
| PUT  | /{id}/permissions | 分配权限 |

### 3.4 PermissionController (`/api/permission`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /tree | 查询权限树 |
| POST | / | 新增权限 |
| PUT  | /{id} | 修改权限 |
| DELETE | /{id} | 删除权限 |

### 3.5 LogController (`/api/log`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | /operation/page | 分页查询操作日志 |
| GET  | /login/page | 分页查询登录日志 |

## 4. UI/UX 规范

### 4.1 色彩体系（深色科技风）
- 主背景色: `#17171a`
- 侧边栏背景: `#1d1d1f`
- 卡片背景: `#232324`
- 主色调: `#165DFF`（Arco Blue）
- 成功色: `#00B42A`
- 警告色: `#FF7D00`
- 危险色: `#F53F3F`
- 文字主色: `#F2F3F5`
- 文字次色: `#A9AEB8`
- 边框色: `#333335`

### 4.2 字体规范
- 主字体: `'PingFang SC', 'Microsoft YaHei', sans-serif`
- 标题字号: 20px / 16px / 14px
- 正文字号: 14px
- 辅助字号: 12px

### 4.3 间距与圆角
- 卡片圆角: 8px
- 按钮圆角: 4px
- 页面内边距: 24px
- 卡片间距: 16px
- 元素间距: 8px / 12px / 16px

### 4.4 阴影
- 卡片阴影: `0 2px 12px rgba(0, 0, 0, 0.3)`
- 弹窗阴影: `0 4px 24px rgba(0, 0, 0, 0.5)`
