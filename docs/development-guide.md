# 风力发电机故障诊断系统 - 开发文档

> 版本：v1.0.0 | 适用对象：开发人员

---

## 一、环境搭建

### 1.1 前置要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | 推荐 Eclipse Temurin |
| Maven | 3.9+ | 后端构建 |
| Node.js | 18+ | 前端构建 |
| MySQL | 8.0 | 数据库 |
| Docker | 24+ | 容器化部署（可选） |
| Docker Compose | 2.20+ | 编排（可选） |

### 1.2 快速启动（Docker）

```bash
# 克隆项目
git clone <repo-url>
cd label-01246

# 一键启动所有服务
docker-compose up --build -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

首次启动约 3-5 分钟（Maven 下载依赖 + npm install）。

### 1.3 本地开发启动

**数据库：**

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE wind_power_diag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入初始化脚本
mysql -u root -p wind_power_diag < sql/schema.sql
```

**后端：**

```bash
cd backend
mvn clean compile
mvn exec:java -Dexec.mainClass="com.windpower.diag.App"
# 或使用 IDE 直接运行 App.java
```

后端启动在 `http://localhost:8080`。

**前端：**

```bash
cd frontend-admin
npm install
npm run dev
```

前端开发服务器在 `http://localhost:5173`，自动代理 `/api` 到后端。

### 1.4 配置文件

**后端配置 `backend/src/main/resources/app.yml`：**

```yaml
server:
  port: 8080

solon.dataSources:
  db1!:
    jdbcUrl: "jdbc:mysql://localhost:3306/wind_power_diag?..."
    username: "root"
    password: "root"

jwt:
  secret: "WindPowerDiagSystem2025SecretKeyForJWTTokenGeneration!@#"
  expiration: 86400000    # 24小时

mail:
  host: "smtp.qq.com"     # SMTP 服务器
  port: 465
  username: "your-email@qq.com"
  password: "your-smtp-password"
  ssl: true

email-verify:
  baseUrl: "http://localhost:8080"
  expireMinutes: 30

csrf:
  allowedOrigins: "http://localhost:5173,http://localhost:8080"
```

**前端代理 `frontend-admin/vite.config.js`：**

```javascript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

---

## 二、项目架构

### 2.1 整体架构

```
浏览器 → Nginx/Vite(前端) → Axios(request.js) → HTTP → 后端 Filter 链 → Controller → Service → Mapper → MySQL
```

### 2.2 后端分层

```
controller/     ← REST API 入口，参数校验，调用 Service
service/        ← 业务逻辑接口
service/impl/   ← 业务逻辑实现
mapper/         ← MyBatis-Plus 数据访问层
entity/         ← 数据库实体类
common/         ← 通用类（Result, PageResult, BusinessException）
config/         ← 配置类（CORS, 数据源, 异常处理, MyBatis-Plus）
interceptor/    ← 过滤器（Auth, XSS, CSRF）
aop/            ← 切面（操作日志）
util/           ← 工具类（JWT, 密码编码, IP, XSS）
```

### 2.3 前端分层

```
views/          ← 页面组件
api/            ← API 接口封装（一个模块一个文件）
store/          ← Pinia 状态管理
router/         ← 路由 + 导航守卫
utils/          ← 工具（request.js, xss.js）
layouts/        ← 布局组件
assets/style/   ← 全局样式 + SCSS 变量
```

### 2.4 请求处理流程

```
请求进入
  ↓
CorsConfig (CORS 跨域处理)
  ↓
XssFilter (XSS 输入过滤 + 安全响应头)
  ↓
CsrfFilter (CSRF Token + Origin 校验)
  ↓
AuthInterceptor (JWT 认证，白名单放行)
  ↓
Controller (业务处理)
  ↓
GlobalExceptionHandler (异常捕获)
  ↓
响应返回
```

---

## 三、后端开发指南

### 3.1 Solon 框架基础

Solon 是轻量级 Java 框架，与 Spring Boot 类似但更轻量。核心注解对照：

| Solon | Spring Boot | 说明 |
|-------|-------------|------|
| `@Component` | `@Component` | 组件注册 |
| `@Controller` | `@RestController` | 控制器 |
| `@Mapping("/path")` | `@RequestMapping` | 路由映射 |
| `@Get` / `@Post` / `@Put` / `@Delete` | 对应 HTTP 方法注解 | HTTP 方法 |
| `@Inject` | `@Autowired` | 依赖注入 |
| `@Param` | `@RequestParam` | 查询参数 |
| `@Path` | `@PathVariable` | 路径参数 |
| `@Body` | `@RequestBody` | 请求体 |
| `@Ds` | - | MyBatis-Plus Mapper 注入（Solon 特有） |
| `Filter` | `Filter` / `HandlerInterceptor` | 过滤器 |

**关键区别：**
- Mapper 注入使用 `@Ds` 而非 `@Inject` 或 `@Autowired`
- 过滤器实现 `Filter` 接口的 `doFilter(Context, FilterChain)` 方法
- 配置注入使用 `@Inject("${key}")` 而非 `@Value`

### 3.2 新增 API 步骤

以新增"设备管理"模块为例：

**1. 创建实体类：**

```java
package com.windpower.diag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("sys_device")
public class SysDevice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceName;
    private String deviceCode;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    // getter/setter...
}
```

**2. 创建 Mapper：**

```java
package com.windpower.diag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.windpower.diag.entity.SysDevice;

public interface SysDeviceMapper extends BaseMapper<SysDevice> {
}
```

**3. 创建 Service 接口和实现：**

```java
// Service 接口
public interface DeviceService {
    PageResult<SysDevice> page(int current, int size, String deviceName);
    void create(SysDevice device);
}

// Service 实现
@Component
public class DeviceServiceImpl implements DeviceService {
    @Ds
    private SysDeviceMapper deviceMapper;

    @Override
    public PageResult<SysDevice> page(int current, int size, String deviceName) {
        LambdaQueryWrapper<SysDevice> wrapper = new LambdaQueryWrapper<>();
        if (deviceName != null && !deviceName.isEmpty()) {
            wrapper.like(SysDevice::getDeviceName, deviceName);
        }
        Page<SysDevice> page = deviceMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public void create(SysDevice device) {
        deviceMapper.insert(device);
    }
}
```

**4. 创建 Controller：**

```java
@Controller
@Mapping("/api/device")
public class DeviceController {
    @Inject
    private DeviceService deviceService;

    @Get
    @Mapping("/page")
    public Result<PageResult<SysDevice>> page(
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String deviceName) {
        return Result.ok(deviceService.page(current, size, deviceName));
    }

    @Post
    @Mapping("/")
    @OperationLog(module = "设备管理", operation = "新增设备")
    public Result<Void> create(@Body SysDevice device) {
        deviceService.create(device);
        return Result.ok("创建成功", null);
    }
}
```

### 3.3 统一响应

所有接口使用 `Result<T>` 包装返回：

```java
Result.ok()                    // 200, "操作成功", null
Result.ok(data)                // 200, "操作成功", data
Result.ok("自定义消息", data)   // 200, "自定义消息", data
Result.fail("错误信息")         // 500, "错误信息", null
Result.fail(400, "参数错误")    // 400, "参数错误", null
```

### 3.4 业务异常

抛出 `BusinessException` 会被全局异常处理器捕获，返回友好错误信息：

```java
throw new BusinessException("用户名已存在");        // code=500
throw new BusinessException(400, "参数不合法");      // code=400
```

### 3.5 操作日志

在 Controller 方法上添加 `@OperationLog` 注解即可自动记录：

```java
@OperationLog(module = "用户管理", operation = "新增用户")
public Result<Void> create(@Body SysUser user) { ... }
```

切面会自动记录：操作用户、模块、操作类型、请求方法/URL/参数、IP、耗时、状态。

### 3.6 MyBatis-Plus 常用操作

```java
// 条件查询
LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(SysUser::getStatus, 1)
       .like(SysUser::getUsername, "admin")
       .orderByDesc(SysUser::getCreatedAt);
List<SysUser> list = userMapper.selectList(wrapper);

// 分页查询
Page<SysUser> page = userMapper.selectPage(new Page<>(1, 10), wrapper);

// 插入
userMapper.insert(user);

// 更新（只更新非 null 字段）
userMapper.updateById(user);

// 逻辑删除（自动设置 deleted=1）
userMapper.deleteById(id);

// 统计
long count = userMapper.selectCount(wrapper);
```

---

## 四、安全机制

### 4.1 认证流程

```
登录请求 → AuthController.login()
  → AuthServiceImpl.login() 验证用户名密码
  → JwtUtil.generateToken() 生成 JWT
  → 返回 token 给前端

后续请求 → AuthInterceptor.doFilter()
  → 从 Authorization 头提取 token
  → JwtUtil.validateToken() 校验
  → 将 userId/username 存入 Context 属性
  → 业务处理
```

### 4.2 XSS 防护

**后端三层防护：**

1. `XssFilter`：全局过滤器，对 POST/PUT 请求的 JSON body 中检测到 XSS 的字符串值进行 HTML 转义
2. `XssUtil.escape()`：`< > " ' &` 五个字符转义为 HTML 实体
3. 安全响应头：`X-Content-Type-Options: nosniff`、`X-XSS-Protection: 1; mode=block`、`X-Frame-Options: DENY`、`Content-Security-Policy`

**前端三层防护：**

1. `request.js` 拦截器：写操作自动调用 `sanitizeObject()` 清理请求数据
2. 表单提交前：`containsXss()` 检测，`stripTags()` 清除标签
3. Vue 模板：`{{ }}` 插值自动转义 HTML；全局 `v-safe-text` 指令

### 4.3 CSRF 防护

**策略：JWT + CSRF Token + Origin 校验**

1. JWT Bearer Token 不会被浏览器自动携带（非 Cookie），天然防 CSRF
2. `CsrfFilter` 额外校验 Origin/Referer 头来源合法性
3. 前端所有写请求携带 `X-CSRF-Token` 和 `X-Requested-With: XMLHttpRequest`
4. 白名单接口（登录/注册等）豁免 CSRF 校验

### 4.4 密码安全

- BCrypt 加密，cost=10，每次加密结果不同（随机盐）
- `PasswordEncoder.encode()` 加密，`matches()` 验证
- 连续 5 次登录失败自动锁定账户

---

## 五、前端开发指南

### 5.1 技术栈

- Vue 3 Composition API + `<script setup>` 语法
- Arco Design Vue 组件库
- Pinia 状态管理
- Axios HTTP 客户端
- SCSS 预处理器（深色科技风主题）

### 5.2 新增页面步骤

**1. 创建视图组件 `src/views/device/index.vue`**

**2. 创建 API 文件 `src/api/device.js`：**

```javascript
import request from '@/utils/request'

export function getDevicePage(params) {
  return request.get('/device/page', { params })
}

export function createDevice(data) {
  return request.post('/device/', data)
}
```

**3. 添加路由 `src/router/index.js`：**

```javascript
{
  path: 'device',
  name: 'DeviceManage',
  component: () => import('@/views/device/index.vue'),
  meta: { title: '设备管理', icon: 'icon-tool' }
}
```

**4. 在数据库中添加菜单权限记录**

### 5.3 请求封装

`src/utils/request.js` 已封装：

- 自动携带 JWT Token（Authorization 头）
- 自动携带 CSRF Token（X-CSRF-Token 头）
- 写操作自动 XSS 清理
- 401 自动跳转登录页
- 403 自动刷新 CSRF Token
- 统一错误提示

### 5.4 权限控制

**路由级别：** 导航守卫从 `userStore.menus` 提取可访问路径，未授权路由重定向到仪表盘。

**按钮级别：** 通过 `userStore.permissions` 判断：

```javascript
const userStore = useUserStore()
const canAdd = userStore.permissions.includes('system:user:add')
```

### 5.5 样式规范

SCSS 变量定义在 `src/assets/style/variables.scss`：

- 主色调：`#165DFF`（科技蓝）
- 背景色：深色系
- 卡片背景：`$bg-card`
- 圆角：`$border-radius-card`
- 阴影：`$shadow-card`

---

## 六、数据库设计

### 6.1 表结构概览

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| sys_role | 角色表 |
| sys_permission | 权限表 |
| sys_user_role | 用户-角色关联表 |
| sys_role_permission | 角色-权限关联表 |
| sys_role_data_scope | 角色数据范围配置表 |
| sys_dept | 部门表 |
| sys_operation_log | 操作日志表 |
| sys_login_log | 登录日志表 |
| sys_email_verify_token | 邮箱验证 Token 表 |

### 6.2 用户状态流转

```
注册 → 状态3(待验证邮箱)
  ↓ 邮箱验证
状态2(待审核)
  ↓ 管理员审核
状态1(启用) ←→ 状态0(禁用)
```

### 6.3 RBAC 权限模型

```
用户(sys_user) ←N:M→ 角色(sys_role) ←N:M→ 权限(sys_permission)
                        ↓
                  数据范围(sys_role_data_scope)
                        ↓
                  部门(sys_dept) → 部门层级树
```

### 6.4 逻辑删除

所有主表使用 `deleted` 字段实现逻辑删除：
- `deleted = 0`：正常
- `deleted = 1`：已删除

MyBatis-Plus 全局配置自动处理，查询自动过滤已删除记录。

---

## 七、邮箱验证流程

### 7.1 流程说明

1. 用户注册时，`AuthServiceImpl.register()` 创建用户（status=3）
2. 调用 `generateAndSaveVerifyToken()` 生成 32 位随机 Token，存入 `sys_email_verify_token` 表
3. 调用 `EmailService.sendVerifyEmail()` 发送 HTML 格式验证邮件
4. 用户点击邮件中的链接，请求 `GET /api/auth/verify-email?token=xxx`
5. `AuthServiceImpl.verifyEmail()` 校验 Token 有效性，标记已使用
6. 更新用户状态：3(待验证) → 2(待审核)，设置 emailVerified=1

### 7.2 配置说明

```yaml
mail:
  host: "smtp.qq.com"       # SMTP 服务器地址
  port: 465                  # SMTP 端口（SSL）
  username: "your@qq.com"   # 发件人邮箱
  password: "smtp-password"  # SMTP 授权码（非邮箱密码）
  ssl: true                  # 启用 SSL
  from: "your@qq.com"       # 发件人地址
  fromName: "风力发电机故障诊断系统"

email-verify:
  baseUrl: "http://localhost:8080"  # 验证链接基础 URL
  expireMinutes: 30                  # Token 有效期（分钟）
```

> QQ 邮箱需在设置中开启 SMTP 服务并获取授权码。

---

## 八、数据范围权限

### 8.1 设计思路

在 RBAC 基础上增加数据范围维度，控制用户可查看的数据范围：

| scopeType | 含义 | 过滤逻辑 |
|-----------|------|----------|
| 1 | 全部数据 | 不加过滤条件 |
| 2 | 本部门数据 | `WHERE dept_id = 用户部门ID` |
| 3 | 本部门及下级 | `WHERE dept_id IN (部门ID + 所有子部门ID)` |
| 4 | 仅本人数据 | `WHERE user_id = 当前用户ID` |

### 8.2 使用方式

在 Service 层调用 `DataScopeService`：

```java
@Inject
private DataScopeService dataScopeService;

// 判断是否仅本人
if (dataScopeService.isSelfOnly(userId)) {
    wrapper.eq(SysUser::getId, userId);
} else {
    List<Long> deptIds = dataScopeService.getVisibleDeptIds(userId);
    if (deptIds != null) {
        wrapper.in(SysUser::getDeptId, deptIds);
    }
    // deptIds == null 表示全部数据
}
```

### 8.3 多角色取最大范围

用户可能有多个角色，取 scopeType 最小值（范围最大）：
- 角色A: scopeType=2（本部门）
- 角色B: scopeType=1（全部）
- 最终: scopeType=1（全部）

---

## 九、Docker 部署

### 9.1 服务编排

```yaml
services:
  mysql:        # MySQL 8.0，自动初始化 schema.sql
  backend:      # Solon 后端，依赖 mysql 健康检查
  frontend-admin:  # Vue 前端 + Nginx，依赖 backend
```

### 9.2 构建命令

```bash
# 构建并启动
docker-compose up --build -d

# 仅重建后端
docker-compose up --build -d backend

# 查看日志
docker-compose logs -f backend

# 停止
docker-compose down

# 停止并清除数据
docker-compose down -v
```

### 9.3 环境配置

Docker 环境使用 `app-docker.yml`，主要区别：
- 数据库地址：`mysql:3306`（容器名）
- CSRF 允许来源包含 `http://localhost:8081`

---

## 十、测试

### 10.1 测试结构

```
src/test/java/com/windpower/diag/
├── util/
│   ├── XssUtilTest.java          # XSS 工具类单元测试
│   ├── PasswordEncoderTest.java  # 密码编码器单元测试
│   └── JwtUtilTest.java          # JWT 工具类单元测试
├── service/
│   ├── DeptServiceTest.java      # 部门服务单元测试（Mockito）
│   └── DataScopeServiceTest.java # 数据范围服务单元测试（Mockito）
└── controller/
    └── ApiIntegrationTest.java   # API 集成测试用例（文档化）
```

### 10.2 运行测试

```bash
cd backend
mvn test
```

### 10.3 测试依赖

- JUnit 5（solon-test-junit5）
- Mockito 5.14（Mock 框架）

---

## 十一、常见问题

### Q1: 后端启动报数据库连接失败

检查 `app.yml` 中数据库地址、用户名、密码是否正确。Docker 环境下数据库地址应为容器名 `mysql`。

### Q2: 前端请求 CORS 报错

确认后端 `CorsConfig` 中 `Access-Control-Allow-Headers` 包含 `X-CSRF-Token` 和 `X-Requested-With`。

### Q3: JWT Token 过期

默认有效期 24 小时（`jwt.expiration: 86400000`），可在 `app.yml` 中调整。

### Q4: 邮件发送失败

1. 确认 SMTP 配置正确（host/port/username/password）
2. QQ 邮箱需使用授权码而非登录密码
3. 检查后端日志中的详细错误信息
4. 邮件发送失败不会阻塞注册流程，但用户无法完成验证

### Q5: Docker 构建慢

Maven 和 npm 首次下载依赖较慢，可配置国内镜像源加速。

### Q6: Mapper 注入为 null

Solon 中 MyBatis-Plus Mapper 必须使用 `@Ds` 注解注入，不能使用 `@Inject`。
