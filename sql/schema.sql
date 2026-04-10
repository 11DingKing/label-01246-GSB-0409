-- ============================================================
-- 风力发电机故障诊断系统 - 数据库初始化脚本
-- Database: MySQL 8.0
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS wind_power_diag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wind_power_diag;

-- -----------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username        VARCHAR(64)  NOT NULL COMMENT '用户名',
    password        VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
    real_name       VARCHAR(64)  DEFAULT NULL COMMENT '真实姓名',
    email           VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    avatar          VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
    dept_id         BIGINT       DEFAULT NULL COMMENT '所属部门ID',
    email_verified  TINYINT      NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证: 0-未验证 1-已验证',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用 2-待审核 3-待验证邮箱',
    account_locked  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否锁定: 0-否 1-是',
    login_fail_count INT         NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip   VARCHAR(64)  DEFAULT NULL COMMENT '最后登录IP',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- -----------------------------------------------------------
-- 2. 角色表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_code   VARCHAR(64)  NOT NULL COMMENT '角色编码',
    role_name   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    description VARCHAR(256) DEFAULT NULL COMMENT '角色描述',
    data_scope  TINYINT      NOT NULL DEFAULT 1 COMMENT '数据范围: 1-全部数据 2-本部门数据 3-本部门及下级 4-仅本人数据 5-自定义',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- -----------------------------------------------------------
-- 3. 权限表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父权限ID, 0为顶级',
    perm_code   VARCHAR(128) NOT NULL COMMENT '权限编码',
    perm_name   VARCHAR(64)  NOT NULL COMMENT '权限名称',
    perm_type   VARCHAR(20)  NOT NULL COMMENT '权限类型: MENU-菜单 BUTTON-按钮 API-接口',
    path        VARCHAR(256) DEFAULT NULL COMMENT '路由路径/接口路径',
    icon        VARCHAR(64)  DEFAULT NULL COMMENT '图标',
    component   VARCHAR(256) DEFAULT NULL COMMENT '前端组件路径',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- -----------------------------------------------------------
-- 4. 用户-角色关联表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- -----------------------------------------------------------
-- 5. 角色-权限关联表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    perm_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, perm_id),
    KEY idx_perm_id (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- -----------------------------------------------------------
-- 6. 操作日志表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT        DEFAULT NULL COMMENT '操作用户ID',
    username    VARCHAR(64)   DEFAULT NULL COMMENT '操作用户名',
    module      VARCHAR(64)   DEFAULT NULL COMMENT '操作模块',
    operation   VARCHAR(128)  DEFAULT NULL COMMENT '操作描述',
    method      VARCHAR(256)  DEFAULT NULL COMMENT '请求方法',
    url         VARCHAR(512)  DEFAULT NULL COMMENT '请求URL',
    params      TEXT          DEFAULT NULL COMMENT '请求参数',
    result      TEXT          DEFAULT NULL COMMENT '返回结果',
    ip          VARCHAR(64)   DEFAULT NULL COMMENT '操作IP',
    duration    INT           DEFAULT NULL COMMENT '耗时(ms)',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
    error_msg   TEXT          DEFAULT NULL COMMENT '错误信息',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- -----------------------------------------------------------
-- 7. 登录日志表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT       DEFAULT NULL COMMENT '用户ID',
    username    VARCHAR(64)  DEFAULT NULL COMMENT '用户名',
    ip          VARCHAR(64)  DEFAULT NULL COMMENT '登录IP',
    user_agent  VARCHAR(512) DEFAULT NULL COMMENT '浏览器UA',
    login_type  TINYINT      NOT NULL DEFAULT 1 COMMENT '类型: 1-登录 2-登出',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
    message     VARCHAR(256) DEFAULT NULL COMMENT '消息',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- -----------------------------------------------------------
-- 8. 邮箱验证Token表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_email_verify_token;
CREATE TABLE sys_email_verify_token (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    token       VARCHAR(128) NOT NULL COMMENT '验证Token',
    expired_at  DATETIME     NOT NULL COMMENT '过期时间',
    used        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已使用: 0-未使用 1-已使用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证Token表';

-- -----------------------------------------------------------
-- 9. 角色数据范围权限表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_role_data_scope;
CREATE TABLE sys_role_data_scope (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id     BIGINT       NOT NULL COMMENT '角色ID',
    scope_type  TINYINT      NOT NULL DEFAULT 1 COMMENT '数据范围类型: 1-全部数据 2-本部门数据 3-本部门及下级 4-仅本人数据 5-自定义',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色数据范围权限表';

-- -----------------------------------------------------------
-- 10. 部门表（数据范围权限依赖）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父部门ID, 0为顶级',
    dept_name   VARCHAR(64)  NOT NULL COMMENT '部门名称',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 初始角色
INSERT INTO sys_role (id, role_code, role_name, description, sort_order) VALUES
(1, 'ADMIN',    '系统管理员',   '拥有系统全部权限', 1),
(2, 'ENGINEER', '运维工程师',   '负责设备运维和故障处理', 2),
(3, 'ANALYST',  '数据分析师',   '负责数据分析和报告生成', 3),
(4, 'VIEWER',   '普通查看用户', '仅拥有查看权限', 4);

-- 初始权限（菜单）
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, component, sort_order) VALUES
(1,  0, 'dashboard',        '数据仪表盘', 'MENU', '/dashboard',        'icon-dashboard',  'dashboard/index', 1),
(2,  0, 'system',           '系统管理',   'MENU', '/system',           'icon-settings',   NULL, 90),
(3,  2, 'system:user',      '用户管理',   'MENU', '/system/user',      'icon-user',       'user/index', 1),
(4,  2, 'system:role',      '角色管理',   'MENU', '/system/role',      'icon-user-group', 'role/index', 2),
(5,  2, 'system:permission','权限管理',   'MENU', '/system/permission','icon-lock',       'permission/index', 3),
(6,  2, 'system:log',       '操作日志',   'MENU', '/system/log',       'icon-file',       'log/index', 4),
-- 按钮权限
(10, 3, 'system:user:add',       '新增用户',   'BUTTON', NULL, NULL, NULL, 1),
(11, 3, 'system:user:edit',      '编辑用户',   'BUTTON', NULL, NULL, NULL, 2),
(12, 3, 'system:user:delete',    '删除用户',   'BUTTON', NULL, NULL, NULL, 3),
(13, 3, 'system:user:assign',    '分配角色',   'BUTTON', NULL, NULL, NULL, 4),
(14, 4, 'system:role:add',       '新增角色',   'BUTTON', NULL, NULL, NULL, 1),
(15, 4, 'system:role:edit',      '编辑角色',   'BUTTON', NULL, NULL, NULL, 2),
(16, 4, 'system:role:delete',    '删除角色',   'BUTTON', NULL, NULL, NULL, 3),
(17, 4, 'system:role:assign',    '分配权限',   'BUTTON', NULL, NULL, NULL, 4),
(18, 5, 'system:perm:add',       '新增权限',   'BUTTON', NULL, NULL, NULL, 1),
(19, 5, 'system:perm:edit',      '编辑权限',   'BUTTON', NULL, NULL, NULL, 2),
(20, 5, 'system:perm:delete',    '删除权限',   'BUTTON', NULL, NULL, NULL, 3),
-- 用户管理扩展按钮
(21, 3, 'system:user:status',    '启用禁用',   'BUTTON', NULL, NULL, NULL, 5),
(22, 3, 'system:user:resetPwd',  '重置密码',   'BUTTON', NULL, NULL, NULL, 6),
(23, 3, 'system:user:unlock',    '解锁用户',   'BUTTON', NULL, NULL, NULL, 7),
-- 角色管理扩展按钮
(24, 4, 'system:role:dataScope',  '数据范围',  'BUTTON', NULL, NULL, NULL, 5),
-- 故障分析按钮权限
(25, 31, 'fault:handle',          '故障处理',  'BUTTON', NULL, NULL, NULL, 1),
-- 系统配置按钮权限
(26, 32, 'system:config:edit',    '编辑配置',  'BUTTON', NULL, NULL, NULL, 1);

-- 管理员拥有全部权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 1, id FROM sys_permission;

-- 运维工程师权限：仪表盘 + 实时监控 + 故障分析(含处理) + 系统管理(父菜单) + 系统配置(含编辑) + 操作日志
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(2, 1), (2, 30), (2, 31), (2, 25), (2, 2), (2, 32), (2, 26), (2, 6);

-- 数据分析师权限：仪表盘 + 实时监控 + 故障分析(只读，无处理按钮) + 系统管理(父菜单) + 操作日志
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(3, 1), (3, 30), (3, 31), (3, 2), (3, 6);

-- 普通查看用户权限：仪表盘 + 实时监控 + 故障分析(只读)
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(4, 1), (4, 30), (4, 31);

-- 初始管理员账号 (密码: admin123)
-- BCrypt hash of 'admin123'
INSERT INTO sys_user (id, username, password, real_name, email, status) VALUES
(1, 'admin', '$2b$10$VNwJEPvNA9u9tE6elL72ve84//9y7n5x6Sg74GytQ2nsYcz9ZVDr.', '系统管理员', 'admin@windpower.com', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始部门
INSERT INTO sys_dept (id, parent_id, dept_name, sort_order) VALUES
(1, 0, '总公司',     1),
(2, 1, '运维部',     1),
(3, 1, '数据分析部', 2),
(4, 1, '技术部',     3);

-- 管理员归属总公司
UPDATE sys_user SET dept_id = 1 WHERE id = 1;

-- 角色数据范围初始化
INSERT INTO sys_role_data_scope (role_id, scope_type) VALUES
(1, 1),  -- 管理员: 全部数据
(2, 3),  -- 运维工程师: 本部门及下级数据
(3, 2),  -- 数据分析师: 本部门数据
(4, 4);  -- 普通查看用户: 仅本人数据

-- ============================================================
-- 风力发电机业务表
-- ============================================================

-- -----------------------------------------------------------
-- 11. 风力发电机设备表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS wind_turbine;
CREATE TABLE wind_turbine (
    id                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    turbine_code          VARCHAR(32)   NOT NULL COMMENT '设备编号',
    turbine_name          VARCHAR(64)   NOT NULL COMMENT '设备名称',
    model                 VARCHAR(64)   DEFAULT NULL COMMENT '设备型号',
    rated_power           DECIMAL(10,2) DEFAULT NULL COMMENT '额定功率(MW)',
    location              VARCHAR(128)  DEFAULT NULL COMMENT '安装位置',
    latitude              DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    longitude             DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    status                TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1-正常运行 2-告警 3-故障 4-停机维护 5-离线',
    dept_id               BIGINT        DEFAULT NULL COMMENT '所属部门ID',
    install_date          DATETIME      DEFAULT NULL COMMENT '安装日期',
    last_maintenance_date DATETIME      DEFAULT NULL COMMENT '最近维护日期',
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted               TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_turbine_code (turbine_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风力发电机设备表';

-- -----------------------------------------------------------
-- 12. 传感器数据表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS turbine_sensor_data;
CREATE TABLE turbine_sensor_data (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    turbine_id    BIGINT        NOT NULL COMMENT '设备ID',
    wind_speed    DECIMAL(8,2)  DEFAULT NULL COMMENT '风速(m/s)',
    rotor_speed   DECIMAL(8,2)  DEFAULT NULL COMMENT '转速(rpm)',
    power         DECIMAL(10,2) DEFAULT NULL COMMENT '功率(kW)',
    nacelle_temp  DECIMAL(6,2)  DEFAULT NULL COMMENT '机舱温度(°C)',
    bearing_temp  DECIMAL(6,2)  DEFAULT NULL COMMENT '轴承温度(°C)',
    gearbox_temp  DECIMAL(6,2)  DEFAULT NULL COMMENT '齿轮箱温度(°C)',
    vibration_x   DECIMAL(8,4)  DEFAULT NULL COMMENT 'X轴振动(mm/s)',
    vibration_y   DECIMAL(8,4)  DEFAULT NULL COMMENT 'Y轴振动(mm/s)',
    vibration_z   DECIMAL(8,4)  DEFAULT NULL COMMENT 'Z轴振动(mm/s)',
    pitch_angle   DECIMAL(6,2)  DEFAULT NULL COMMENT '桨距角(°)',
    yaw_angle     DECIMAL(6,2)  DEFAULT NULL COMMENT '偏航角(°)',
    record_time   DATETIME      NOT NULL COMMENT '采集时间',
    PRIMARY KEY (id),
    KEY idx_turbine_time (turbine_id, record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='传感器数据表';

-- -----------------------------------------------------------
-- 13. 故障记录表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS fault_record;
CREATE TABLE fault_record (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    turbine_id   BIGINT        NOT NULL COMMENT '设备ID',
    fault_code   VARCHAR(32)   NOT NULL COMMENT '故障编号',
    fault_type   VARCHAR(64)   DEFAULT NULL COMMENT '故障类型',
    fault_level  VARCHAR(20)   DEFAULT NULL COMMENT '故障等级: LOW/MEDIUM/HIGH/CRITICAL',
    description  TEXT          DEFAULT NULL COMMENT '故障描述',
    diagnosis    TEXT          DEFAULT NULL COMMENT '诊断分析',
    solution     TEXT          DEFAULT NULL COMMENT '处理方案',
    status       TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1-待处理 2-处理中 3-已解决 4-已关闭',
    handler      VARCHAR(64)   DEFAULT NULL COMMENT '处理人',
    fault_time   DATETIME      DEFAULT NULL COMMENT '故障发生时间',
    resolve_time DATETIME      DEFAULT NULL COMMENT '解决时间',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fault_code (fault_code),
    KEY idx_turbine_id (turbine_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='故障记录表';

-- -----------------------------------------------------------
-- 14. 系统配置表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    config_group  VARCHAR(64)  NOT NULL COMMENT '配置分组',
    config_key    VARCHAR(128) NOT NULL COMMENT '配置键',
    config_value  VARCHAR(512) DEFAULT NULL COMMENT '配置值',
    config_name   VARCHAR(128) DEFAULT NULL COMMENT '配置名称',
    description   VARCHAR(256) DEFAULT NULL COMMENT '描述',
    value_type    VARCHAR(20)  DEFAULT 'STRING' COMMENT '值类型: STRING/NUMBER/BOOLEAN/JSON',
    sort_order    INT          NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 业务初始数据
-- ============================================================

-- 风力发电机设备
INSERT INTO wind_turbine (id, turbine_code, turbine_name, model, rated_power, location, status, install_date) VALUES
(1, 'WT-001', '1号风机', 'GW-3.0MW', 3.00, '风电场A区-01', 1, '2023-06-15 00:00:00'),
(2, 'WT-002', '2号风机', 'GW-3.0MW', 3.00, '风电场A区-02', 1, '2023-06-15 00:00:00'),
(3, 'WT-003', '3号风机', 'GW-3.0MW', 3.00, '风电场A区-03', 1, '2023-07-20 00:00:00'),
(4, 'WT-004', '4号风机', 'GW-2.5MW', 2.50, '风电场B区-01', 1, '2023-08-10 00:00:00'),
(5, 'WT-005', '5号风机', 'GW-2.5MW', 2.50, '风电场B区-02', 2, '2023-08-10 00:00:00'),
(6, 'WT-006', '6号风机', 'GW-3.0MW', 3.00, '风电场B区-03', 1, '2023-09-01 00:00:00'),
(7, 'WT-007', '7号风机', 'GW-2.5MW', 2.50, '风电场C区-01', 3, '2024-01-15 00:00:00'),
(8, 'WT-008', '8号风机', 'GW-3.0MW', 3.00, '风电场C区-02', 1, '2024-01-15 00:00:00');

-- 故障记录示例
INSERT INTO fault_record (turbine_id, fault_code, fault_type, fault_level, description, diagnosis, solution, status, handler, fault_time, resolve_time) VALUES
(7, 'FLT-2026-0001', '齿轮箱故障', 'HIGH', '齿轮箱异常振动，温度持续升高', '经振动频谱分析，齿轮箱内部齿轮存在点蚀磨损，导致啮合异常。温度传感器显示齿轮箱油温超过70°C阈值。', '已更换受损齿轮组并更换润滑油，设备恢复正常运行。', 3, '张工', '2026-02-10 08:30:00', '2026-02-10 14:20:00'),
(5, 'FLT-2026-0002', '发电机故障', 'MEDIUM', '发电机输出功率波动异常', '发电机绕组绝缘电阻下降，可能存在局部绝缘老化。功率因数偏低，谐波含量增大。', NULL, 2, '李工', '2026-02-18 10:15:00', NULL),
(3, 'FLT-2026-0003', '叶片故障', 'LOW', '叶片前缘轻微裂纹', '目视检查发现3号叶片前缘存在约15cm裂纹，暂不影响运行安全，建议下次定期维护时修复。', NULL, 1, NULL, '2026-02-20 16:00:00', NULL),
(7, 'FLT-2026-0004', '偏航系统', 'CRITICAL', '偏航电机无法响应，风机无法对风', '偏航驱动器控制板故障，导致偏航电机无法接收指令。风机持续偏离风向超过30°，发电效率严重下降。', '紧急更换偏航驱动器控制板，重新校准偏航角度，设备恢复正常对风。', 3, '王工', '2026-02-05 06:45:00', '2026-02-05 12:30:00'),
(2, 'FLT-2026-0005', '变桨系统', 'MEDIUM', '变桨角度响应迟缓', '变桨液压系统压力不足，液压油存在泄漏。变桨响应时间从正常的2秒延长至5秒。', NULL, 2, '张工', '2026-02-19 09:00:00', NULL),
(6, 'FLT-2026-0006', '电气系统', 'LOW', '变流器散热风扇异响', '变流器散热风扇轴承磨损，产生异常噪音。散热效率略有下降但仍在安全范围内。', '已更换散热风扇，噪音消除。', 4, '李工', '2026-02-01 14:30:00', '2026-02-01 16:00:00');

-- 系统配置初始数据
INSERT INTO sys_config (config_group, config_key, config_value, config_name, description, value_type, sort_order) VALUES
('alarm', 'wind_speed_max', '25.0', '风速上限', '风速超过此值触发告警(m/s)', 'NUMBER', 1),
('alarm', 'wind_speed_min', '3.0', '风速下限', '风速低于此值触发告警(m/s)', 'NUMBER', 2),
('alarm', 'nacelle_temp_max', '65.0', '机舱温度上限', '机舱温度超过此值触发告警(°C)', 'NUMBER', 3),
('alarm', 'bearing_temp_max', '75.0', '轴承温度上限', '轴承温度超过此值触发告警(°C)', 'NUMBER', 4),
('alarm', 'gearbox_temp_max', '70.0', '齿轮箱温度上限', '齿轮箱温度超过此值触发告警(°C)', 'NUMBER', 5),
('alarm', 'vibration_max', '4.5', '振动幅值上限', '振动超过此值触发告警(mm/s)', 'NUMBER', 6),
('collect', 'collect_interval', '10', '采集频率', '传感器数据采集间隔(秒)', 'NUMBER', 1),
('collect', 'report_interval', '60', '上报间隔', '数据上报到服务器的间隔(秒)', 'NUMBER', 2),
('collect', 'retention_days', '365', '数据保留天数', '历史传感器数据保留天数', 'NUMBER', 3),
('collect', 'compression', 'true', '数据压缩', '是否启用历史数据压缩', 'BOOLEAN', 4),
('notify', 'email_enabled', 'true', '邮件通知', '是否启用邮件告警通知', 'BOOLEAN', 1),
('notify', 'sms_enabled', 'false', '短信通知', '是否启用短信告警通知', 'BOOLEAN', 2),
('notify', 'notify_email', '', '通知邮箱', '告警通知接收邮箱', 'STRING', 3),
('notify', 'notify_phone', '', '通知手机号', '告警通知接收手机号', 'STRING', 4),
('notify', 'silence_period', '30', '静默时间', '告警静默时间(分钟)', 'NUMBER', 5),
('notify', 'repeat_interval', '60', '重复间隔', '重复告警通知间隔(分钟)', 'NUMBER', 6),
('system', 'system_name', '风力发电机故障诊断系统', '系统名称', '系统显示名称', 'STRING', 1),
('system', 'session_timeout', '120', '会话超时', '用户会话超时时间(分钟)', 'NUMBER', 2),
('system', 'login_fail_lock', '5', '登录锁定次数', '连续登录失败锁定账户的次数', 'NUMBER', 3),
('system', 'password_min_length', '6', '密码最小长度', '用户密码最小长度要求', 'NUMBER', 4),
('system', 'log_retention_days', '180', '日志保留天数', '操作日志保留天数', 'NUMBER', 5),
('system', 'register_enabled', 'true', '启用注册', '是否开放用户注册功能', 'BOOLEAN', 6);

-- 新增权限菜单：实时监控、故障分析、系统配置
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, component, sort_order) VALUES
(30, 0, 'monitor',       '实时监控', 'MENU', '/monitor', 'icon-computer', 'monitor/index', 2),
(31, 0, 'fault',         '故障分析', 'MENU', '/fault',   'icon-bug',      'fault/index',   3),
(32, 2, 'system:config', '系统配置', 'MENU', '/system/config', 'icon-settings', 'config/index', 5);

-- 管理员追加新权限（实时监控、故障分析、系统配置及所有新增按钮权限）
INSERT INTO sys_role_permission (role_id, perm_id) VALUES (1, 30), (1, 31), (1, 32), (1, 21), (1, 22), (1, 23), (1, 24), (1, 25), (1, 26);

-- 传感器模拟数据（最近24小时，每小时一条，设备WT-001）
INSERT INTO turbine_sensor_data (turbine_id, wind_speed, rotor_speed, power, nacelle_temp, bearing_temp, gearbox_temp, vibration_x, vibration_y, vibration_z, pitch_angle, yaw_angle, record_time) VALUES
(1, 8.5, 12.3, 1250.0, 42.5, 48.2, 52.1, 1.2, 0.8, 0.5, 5.2, 178.5, DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(1, 9.2, 13.1, 1380.0, 43.1, 49.0, 53.2, 1.3, 0.9, 0.6, 5.5, 180.2, DATE_SUB(NOW(), INTERVAL 22 HOUR)),
(1, 10.1, 14.0, 1520.0, 44.2, 50.1, 54.0, 1.4, 1.0, 0.6, 6.0, 179.8, DATE_SUB(NOW(), INTERVAL 21 HOUR)),
(1, 11.5, 15.2, 1780.0, 45.8, 51.5, 55.3, 1.5, 1.1, 0.7, 6.8, 181.0, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
(1, 12.0, 15.8, 1900.0, 46.5, 52.3, 56.1, 1.6, 1.2, 0.7, 7.2, 180.5, DATE_SUB(NOW(), INTERVAL 19 HOUR)),
(1, 10.8, 14.5, 1600.0, 45.0, 50.8, 54.5, 1.4, 1.0, 0.6, 6.3, 179.2, DATE_SUB(NOW(), INTERVAL 18 HOUR)),
(1, 9.5, 13.5, 1420.0, 43.8, 49.5, 53.8, 1.3, 0.9, 0.5, 5.8, 178.8, DATE_SUB(NOW(), INTERVAL 17 HOUR)),
(1, 7.8, 11.5, 1050.0, 41.2, 47.0, 51.0, 1.1, 0.7, 0.4, 4.5, 177.5, DATE_SUB(NOW(), INTERVAL 16 HOUR)),
(1, 6.5, 10.2, 850.0, 39.5, 45.2, 49.5, 0.9, 0.6, 0.4, 3.8, 176.0, DATE_SUB(NOW(), INTERVAL 15 HOUR)),
(1, 7.2, 11.0, 980.0, 40.5, 46.5, 50.2, 1.0, 0.7, 0.4, 4.2, 177.0, DATE_SUB(NOW(), INTERVAL 14 HOUR)),
(1, 8.8, 12.8, 1300.0, 42.8, 48.8, 52.5, 1.2, 0.8, 0.5, 5.3, 179.0, DATE_SUB(NOW(), INTERVAL 13 HOUR)),
(1, 11.0, 14.8, 1700.0, 45.5, 51.2, 55.0, 1.5, 1.1, 0.7, 6.5, 180.8, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(1, 13.2, 16.5, 2100.0, 48.0, 54.0, 58.2, 1.8, 1.3, 0.8, 8.0, 182.0, DATE_SUB(NOW(), INTERVAL 11 HOUR)),
(1, 14.5, 17.2, 2350.0, 50.2, 56.5, 60.1, 2.0, 1.5, 0.9, 9.2, 183.5, DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(1, 13.8, 16.8, 2200.0, 49.0, 55.2, 59.0, 1.9, 1.4, 0.8, 8.5, 182.8, DATE_SUB(NOW(), INTERVAL 9 HOUR)),
(1, 12.5, 15.5, 1950.0, 47.2, 53.0, 57.0, 1.7, 1.2, 0.7, 7.5, 181.5, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(1, 10.5, 14.2, 1550.0, 44.5, 50.5, 54.2, 1.4, 1.0, 0.6, 6.2, 179.5, DATE_SUB(NOW(), INTERVAL 7 HOUR)),
(1, 9.0, 13.0, 1350.0, 43.0, 49.2, 53.0, 1.3, 0.9, 0.5, 5.5, 178.5, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(1, 8.0, 12.0, 1150.0, 41.8, 47.5, 51.5, 1.1, 0.8, 0.5, 4.8, 177.8, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(1, 7.5, 11.2, 1000.0, 40.8, 46.8, 50.8, 1.0, 0.7, 0.4, 4.3, 177.2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(1, 8.2, 12.5, 1200.0, 42.0, 48.0, 51.8, 1.2, 0.8, 0.5, 5.0, 178.2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 9.8, 13.8, 1480.0, 44.0, 50.0, 53.5, 1.3, 0.9, 0.6, 5.8, 179.5, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 10.2, 14.2, 1560.0, 44.5, 50.5, 54.2, 1.4, 1.0, 0.6, 6.0, 180.0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 9.5, 13.5, 1400.0, 43.5, 49.5, 53.5, 1.3, 0.9, 0.5, 5.6, 179.0, NOW());
