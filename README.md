# 风力发电机故障诊断系统

## How to Run

```bash
# 克隆项目后，在项目根目录执行：
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并清除数据卷（重置数据库）
docker-compose down -v
```

> 首次启动需要编译后端和前端，预计耗时 3-5 分钟。MySQL 初始化脚本会自动执行建表和数据导入。

## Services

| 服务 | 容器名 | 端口映射 | 说明 |
|------|--------|----------|------|
| MySQL 8.0 | windpower-mysql | 3306:3306 | 数据库，自动初始化 |
| 后端 API | windpower-backend | 8082:8082 | Solon 3.7.3 + JDK 21 |
| 管理后台 | windpower-frontend-admin | 8081:80 | Vue 3 + Arco Design Pro |

访问地址：
- 管理后台：http://localhost:8081
- 后端 API：http://localhost:8082

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 系统管理员 | admin | admin123 |

> 注册新用户默认为"待审核"状态，需管理员在用户管理中审核通过后方可登录。

## 题目内容
设计并开发一个功能完善的"风力发电机故障诊断系统"，该系统需通过实时采集、分析风力发电机的运行数据，实现故障的早期预警、精确定位及智能处理，从而显著提升故障处理效率，最大限度减少设备停机时间，保障风力发电系统的持续稳定运行。项目开发需遵循以下详细规范：

### 系统总体架构要求
1. **前端开发**：基于VUE版的Arco Design Pro组件库构建直观、高效的用户界面，需包含数据可视化仪表盘、实时监控界面、故障分析报告展示、系统配置等核心模块。确保界面设计符合工业软件使用习惯，支持多屏幕分辨率自适应，确保界面设计符合工业软件使用习惯，支持多屏幕分辨率自适应（兼容1080p至4K分辨率），界面设计科技化（采用深色主题为主，蓝色科技感配色方案）。

2. **后端服务**：采用Solon 3.7.3框架结合JDK 21开发RESTful API服务，实现业务逻辑处理、数据访问控制、第三方系统集成等功能。

3. **数据存储**：使用MySQL 8.0关系型数据库存储系统配置、用户信息、故障记录、设备参数等结构化数据。

### 第一阶段开发任务：用户与权限管理系统
1. **用户认证与授权**：
- 实现基于RBAC(基于角色的访问控制)模型的权限管理系统
- 开发安全的用户登录功能，支持密码加密存储、登录日志记录
- 实现用户注册功能，包含邮箱验证、信息审核流程
- 支持用户密码重置、账户锁定与解锁功能

2. **角色与权限设计**：
- 预设系统管理员、运维工程师、数据分析师、普通查看用户等角色
- 实现细粒度的权限控制，支持功能模块权限、数据范围权限、操作权限的灵活配置
- 开发权限分配界面，支持角色创建、权限分配、用户角色关联管理

3. **安全要求**：
- 实现API接口的身份认证与授权校验
- 防止SQL注入、XSS攻击、CSRF攻击等常见安全威胁
- 实现操作日志记录功能，记录用户关键操作行为

4. **交付成果**：
- 完整的用户与权限管理模块源代码
- 数据库设计文档及SQL脚本
- API接口文档及测试用例
- 用户操作手册及开发文档

系统开发需遵循模块化、可扩展、可维护的设计原则，采用敏捷开发方法，确保代码质量和系统性能。
---

## 项目介绍

风力发电机故障诊断系统，已完成第一阶段（用户与权限管理 RBAC）和第二阶段（设备监控、故障诊断、系统配置）开发。

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Arco Design Pro | Vue 3.5 / Arco 2.56 |
| 构建 | Vite | 6.x |
| 状态管理 | Pinia | 2.3 |
| 后端 | Solon | 3.7.3 |
| JDK | Eclipse Temurin | 21 |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8.0 |
| 图表 | ECharts (vue-echarts) | 7.x |
| 认证 | JWT (jjwt) | 0.12.6 |
| 容器化 | Docker + Docker Compose | - |

### 项目结构

```
├── docker-compose.yml                          # Docker 编排
├── .gitignore
├── README.md
├── docs/
│   ├── project_design.md                       # 设计文档
│   ├── api-docs.md                             # API 接口文档
│   ├── user-manual.md                          # 用户操作手册
│   └── development-guide.md                    # 开发文档
├── sql/
│   └── schema.sql                              # 数据库初始化脚本
├── backend/                                    # 后端服务
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   └── src/main/
│       ├── resources/
│       │   ├── app.yml                         # 默认配置（本地开发）
│       │   ├── app-docker.yml                  # Docker 环境配置
│       │   └── logback.xml                     # 日志配置
│       └── java/com/windpower/diag/
│           ├── App.java                        # 启动类
│           ├── aop/
│           │   ├── OperationLog.java           # 操作日志注解
│           │   └── OperationLogAspect.java     # 操作日志切面
│           ├── common/
│           │   ├── BusinessException.java      # 业务异常
│           │   ├── PageResult.java             # 分页结果
│           │   └── Result.java                 # 统一响应
│           ├── config/
│           │   ├── CorsConfig.java             # CORS 跨域
│           │   ├── DataSourceConfig.java       # 数据源（Solon 自动管理）
│           │   ├── GlobalExceptionHandler.java # 全局异常处理
│           │   └── MybatisPlusConfig.java      # MyBatis-Plus 配置
│           ├── controller/
│           │   ├── AuthController.java         # 认证：登录/注册/密码
│           │   ├── ConfigController.java       # 系统配置 CRUD
│           │   ├── FaultController.java        # 故障记录查询/统计
│           │   ├── HealthController.java       # 健康检查
│           │   ├── LogController.java          # 日志查询
│           │   ├── PermissionController.java   # 权限 CRUD
│           │   ├── RoleController.java         # 角色 CRUD
│           │   ├── TurbineController.java      # 设备管理/监控数据
│           │   └── UserController.java         # 用户 CRUD
│           ├── entity/
│           │   ├── SysUser.java
│           │   ├── SysRole.java
│           │   ├── SysPermission.java
│           │   ├── SysUserRole.java
│           │   ├── SysRolePermission.java
│           │   ├── SysOperationLog.java
│           │   ├── SysLoginLog.java
│           │   ├── SysConfig.java              # 系统配置实体
│           │   ├── WindTurbine.java             # 风力发电机设备实体
│           │   ├── TurbineSensorData.java       # 传感器数据实体
│           │   └── FaultRecord.java             # 故障记录实体
│           ├── mapper/
│           │   ├── SysUserMapper.java
│           │   ├── SysRoleMapper.java
│           │   ├── SysPermissionMapper.java
│           │   ├── SysUserRoleMapper.java
│           │   ├── SysRolePermissionMapper.java
│           │   ├── SysOperationLogMapper.java
│           │   ├── SysLoginLogMapper.java
│           │   ├── SysConfigMapper.java
│           │   ├── WindTurbineMapper.java
│           │   ├── TurbineSensorDataMapper.java
│           │   └── FaultRecordMapper.java
│           ├── service/
│           │   ├── AuthService.java
│           │   ├── UserService.java
│           │   ├── RoleService.java
│           │   ├── PermissionService.java
│           │   ├── LogService.java
│           │   ├── ConfigService.java
│           │   ├── TurbineService.java
│           │   ├── FaultService.java
│           │   └── impl/
│           │       ├── AuthServiceImpl.java
│           │       ├── UserServiceImpl.java
│           │       ├── RoleServiceImpl.java
│           │       ├── PermissionServiceImpl.java
│           │       ├── LogServiceImpl.java
│           │       ├── ConfigServiceImpl.java
│           │       ├── TurbineServiceImpl.java
│           │       └── FaultServiceImpl.java
│           ├── interceptor/
│           │   ├── AuthInterceptor.java        # JWT 认证拦截器
│           │   └── XssFilter.java              # XSS 防护过滤器
│           └── util/
│               ├── JwtUtil.java
│               ├── PasswordEncoder.java
│               └── IpUtil.java
└── frontend-admin/                             # 管理后台前端
    ├── Dockerfile
    ├── .dockerignore
    ├── nginx.conf                              # Nginx（SPA 路由 + API 反向代理）
    ├── package.json
    ├── vite.config.js
    ├── index.html
    ├── public/
    │   └── favicon.svg
    └── src/
        ├── main.js                             # 入口文件
        ├── App.vue                             # 根组件
        ├── api/
        │   ├── auth.js                         # 认证接口
        │   ├── user.js                         # 用户接口
        │   ├── role.js                         # 角色接口
        │   ├── permission.js                   # 权限接口
        │   ├── log.js                          # 日志接口
        │   ├── turbine.js                      # 设备/监控/仪表盘接口
        │   ├── fault.js                        # 故障记录/统计接口
        │   └── config.js                       # 系统配置接口
        ├── assets/style/
        │   ├── variables.scss                  # SCSS 变量（深色科技风）
        │   └── global.scss                     # 全局样式
        ├── layouts/
        │   └── MainLayout.vue                  # 主布局（侧边栏+顶栏+修改密码）
        ├── router/
        │   └── index.js                        # 路由 + 导航守卫
        ├── store/
        │   └── user.js                         # Pinia 用户状态
        ├── utils/
        │   └── request.js                      # Axios 封装
        └── views/
            ├── login/index.vue                 # 登录 / 注册
            ├── dashboard/index.vue             # 数据可视化仪表盘（风速功率趋势、设备状态分布、故障统计图表）
            ├── monitor/index.vue               # 实时监控（传感器数据、趋势图表、设备列表）
            ├── fault/index.vue                 # 故障分析报告（故障统计、类型/等级图表、记录列表、诊断详情）
            ├── config/index.vue                # 系统配置（告警阈值、数据采集、通知设置、系统参数）
            ├── user/index.vue                  # 用户管理
            ├── role/index.vue                  # 角色管理
            ├── permission/index.vue            # 权限管理
            └── log/index.vue                   # 操作日志 / 登录日志
```

### 功能清单

#### 数据可视化仪表盘（第二阶段新增）
| 功能 | 接口 | 说明 |
|------|------|------|
| 仪表盘统计 | GET /api/turbine/dashboard-stats | 设备总数、运行/告警/故障数、总装机容量 |
| 风速功率趋势 | - | ECharts 双Y轴折线图，展示24小时风速与功率趋势 |
| 设备状态分布 | - | 饼图展示正常/告警/故障/停机/离线设备占比 |
| 故障类型统计 | GET /api/fault/statistics | 柱状图展示各类故障数量 |
| 故障等级分布 | - | 南丁格尔玫瑰图展示故障等级占比 |
| 快捷操作 | - | 根据用户权限动态展示可访问的功能入口 |

#### 实时监控（第二阶段新增）
| 功能 | 接口 | 说明 |
|------|------|------|
| 设备列表 | GET /api/turbine/list | 全量设备列表，展示编号/名称/型号/功率/状态 |
| 实时监控数据 | GET /api/turbine/monitor | 传感器实时数据（风速/转速/功率/温度/振动） |
| 风速转速趋势 | - | 折线图展示24小时风速与转速变化 |
| 温度监测 | - | 折线图展示机舱/轴承/齿轮箱温度趋势 |
| 振动监测 | - | 折线图展示X/Y/Z三轴振动数据 |
| 功率输出 | - | 面积图展示功率输出趋势 |
| 自动刷新 | - | 每30秒自动刷新监控数据 |

#### 故障分析（第二阶段新增）
| 功能 | 接口 | 说明 |
|------|------|------|
| 故障记录分页 | GET /api/fault/page | 支持按故障类型、等级、状态、设备筛选 |
| 故障详情 | GET /api/fault/{id} | 查看故障诊断报告详情 |
| 故障统计 | GET /api/fault/statistics | 故障总数、待处理/处理中/已解决统计 |
| 故障类型分布 | - | 横向柱状图展示各类故障数量 |
| 故障等级占比 | - | 环形图展示LOW/MEDIUM/HIGH/CRITICAL占比 |
| 诊断报告弹窗 | - | 展示故障编号、设备、类型、等级、诊断分析、处理方案 |

#### 系统配置（第二阶段新增）
| 功能 | 接口 | 说明 |
|------|------|------|
| 配置列表查询 | GET /api/config/list | 支持按分组筛选，返回全部配置项 |
| 修改配置项 | PUT /api/config/{id} | 修改单个配置值 |
| 批量修改配置 | PUT /api/config/batch | 批量更新配置项 |
| 告警阈值配置 | - | 风速/温度/振动阈值设置 |
| 数据采集配置 | - | 采集频率、上报间隔、数据保留天数 |
| 通知设置 | - | 邮件/短信通知开关、接收人、静默时间 |
| 系统参数 | - | 系统名称、会话超时、登录锁定、密码策略 |

#### 设备管理（第二阶段新增）
| 功能 | 接口 | 说明 |
|------|------|------|
| 设备分页查询 | GET /api/turbine/page | 支持按设备编号、状态筛选 |
| 设备列表（全量） | GET /api/turbine/list | 用于下拉选择 |
| 设备详情 | GET /api/turbine/{id} | 查看设备详细信息 |

#### 用户认证
| 功能 | 接口 | 说明 |
|------|------|------|
| 用户登录 | POST /api/auth/login | JWT Token 认证，返回 token + 用户信息 |
| 用户注册 | POST /api/auth/register | 注册后状态为"待审核"，需管理员审核 |
| 获取当前用户信息 | GET /api/auth/info | 返回用户信息、角色列表、权限列表 |
| 修改密码 | PUT /api/auth/password | 需验证原密码 |
| 重置密码 | POST /api/auth/reset-password | 管理员操作，重置为默认密码 123456 |
| 退出登录 | POST /api/auth/logout | 清除登录状态 |
| 登录失败锁定 | - | 连续失败 5 次自动锁定账户 |

#### 用户管理
| 功能 | 接口 | 说明 |
|------|------|------|
| 用户分页查询 | GET /api/user/page | 支持按用户名、姓名、状态筛选 |
| 查询用户详情 | GET /api/user/{id} | - |
| 新增用户 | POST /api/user/ | - |
| 编辑用户 | PUT /api/user/{id} | - |
| 删除用户 | DELETE /api/user/{id} | 逻辑删除 |
| 启用/禁用用户 | PUT /api/user/{id}/status | 切换用户状态 |
| 锁定/解锁用户 | PUT /api/user/{id}/lock | - |
| 分配角色 | PUT /api/user/{id}/roles | 多选角色分配 |
| 查询用户角色 | GET /api/user/{id}/roles | - |

#### 角色管理
| 功能 | 接口 | 说明 |
|------|------|------|
| 角色分页查询 | GET /api/role/page | 支持按角色名称、状态筛选 |
| 角色列表（全量） | GET /api/role/list | 用于下拉选择 |
| 新增角色 | POST /api/role/ | - |
| 编辑角色 | PUT /api/role/{id} | - |
| 删除角色 | DELETE /api/role/{id} | - |
| 分配权限 | PUT /api/role/{id}/permissions | 树形多选权限分配 |
| 查询角色权限 | GET /api/role/{id}/permissions | - |

#### 权限管理
| 功能 | 接口 | 说明 |
|------|------|------|
| 权限树查询 | GET /api/permission/tree | 返回树形结构 |
| 新增权限 | POST /api/permission/ | 支持菜单/按钮/接口三种类型 |
| 编辑权限 | PUT /api/permission/{id} | - |
| 删除权限 | DELETE /api/permission/{id} | - |

#### 日志管理
| 功能 | 接口 | 说明 |
|------|------|------|
| 操作日志分页查询 | GET /api/log/operation/page | 支持按用户名、模块筛选 |
| 登录日志分页查询 | GET /api/log/login/page | 支持按用户名、状态筛选 |

#### 安全与基础设施
| 功能 | 说明 |
|------|------|
| JWT 认证拦截 | 全局 Filter，白名单放行登录/注册接口 |
| BCrypt 密码加密 | 使用 favre BCrypt 库，cost=10 |
| XSS 防护 | XssFilter 全局过滤，防止跨站脚本攻击 |
| 操作日志 AOP | @OperationLog 注解 + 切面自动记录 |
| 全局异常处理 | BusinessException / IllegalArgumentException / Throwable 三级捕获 |
| CORS 跨域 | 全局 Filter，允许所有来源 |
| 健康检查 | GET / 返回服务状态 |

### 预设角色

| 角色编码 | 角色名称 | 预分配权限 |
|----------|----------|------------|
| ADMIN | 系统管理员 | 全部权限（含设备监控、故障分析、系统配置） |
| ENGINEER | 运维工程师 | 数据仪表盘、实时监控、故障分析、系统配置、操作日志 |
| ANALYST | 数据分析师 | 数据仪表盘、实时监控、故障分析、操作日志 |
| VIEWER | 普通查看用户 | 数据仪表盘、实时监控、故障分析（只读） |

### 预设权限树

```
├── 数据仪表盘 (dashboard)                    [MENU]
├── 实时监控 (monitor)                        [MENU]
├── 故障分析 (fault)                          [MENU]
│   └── 故障处理 (fault:handle)               [BUTTON]
└── 系统管理 (system)                          [MENU]
    ├── 用户管理 (system:user)                 [MENU]
    │   ├── 新增用户 (system:user:add)         [BUTTON]
    │   ├── 编辑用户 (system:user:edit)        [BUTTON]
    │   ├── 删除用户 (system:user:delete)      [BUTTON]
    │   ├── 分配角色 (system:user:assign)      [BUTTON]
    │   ├── 启用禁用 (system:user:status)      [BUTTON]
    │   ├── 重置密码 (system:user:resetPwd)    [BUTTON]
    │   └── 解锁用户 (system:user:unlock)      [BUTTON]
    ├── 角色管理 (system:role)                 [MENU]
    │   ├── 新增角色 (system:role:add)         [BUTTON]
    │   ├── 编辑角色 (system:role:edit)        [BUTTON]
    │   ├── 删除角色 (system:role:delete)      [BUTTON]
    │   ├── 分配权限 (system:role:assign)      [BUTTON]
    │   └── 数据范围 (system:role:dataScope)   [BUTTON]
    ├── 权限管理 (system:permission)           [MENU]
    │   ├── 新增权限 (system:perm:add)         [BUTTON]
    │   ├── 编辑权限 (system:perm:edit)        [BUTTON]
    │   └── 删除权限 (system:perm:delete)      [BUTTON]
    ├── 系统配置 (system:config)               [MENU]
    │   └── 编辑配置 (system:config:edit)      [BUTTON]
    └── 操作日志 (system:log)                  [MENU]
```

### 数据库表结构

| 序号 | 表名 | 说明 | 阶段 |
|------|------|------|------|
| 1 | sys_user | 系统用户表 | 第一阶段 |
| 2 | sys_role | 系统角色表 | 第一阶段 |
| 3 | sys_permission | 系统权限表 | 第一阶段 |
| 4 | sys_user_role | 用户角色关联表 | 第一阶段 |
| 5 | sys_role_permission | 角色权限关联表 | 第一阶段 |
| 6 | sys_operation_log | 操作日志表 | 第一阶段 |
| 7 | sys_login_log | 登录日志表 | 第一阶段 |
| 8 | sys_email_verify_token | 邮箱验证Token表 | 第一阶段 |
| 9 | sys_role_data_scope | 角色数据范围权限表 | 第一阶段 |
| 10 | sys_dept | 部门表 | 第一阶段 |
| 11 | wind_turbine | 风力发电机设备表（编号/名称/型号/功率/位置/状态） | 第二阶段 |
| 12 | turbine_sensor_data | 传感器数据表（风速/转速/功率/温度/振动/桨距角/偏航角） | 第二阶段 |
| 13 | fault_record | 故障记录表（故障编号/类型/等级/诊断/方案/状态） | 第二阶段 |
| 14 | sys_config | 系统配置表（分组/键值/类型） | 第二阶段 |
