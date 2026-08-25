-- PostgreSQL V1.0.0 全量基础系统表初始化脚本与预置种子数据
-- 包含了：用户、角色、用户角色关联、菜单与权限、角色菜单关联、数据字典类型、多级数据字典、登录日志、操作日志全套基座表及种子数据

-- =================================================================
-- 1. DDL 建表语句
-- =================================================================

-- 1.1 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(64),
    mobile VARCHAR(20),
    status INT NOT NULL DEFAULT 1,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag INT NOT NULL DEFAULT 0
);

-- 1.2 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag INT NOT NULL DEFAULT 0
);

-- 1.3 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 1.4 菜单与权限标识表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(64) NOT NULL,
    path VARCHAR(255),
    component VARCHAR(255),
    permission VARCHAR(100),
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    menu_type INT NOT NULL DEFAULT 0, -- 0-目录 1-菜单 2-按钮
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag INT NOT NULL DEFAULT 0
);

-- 1.5 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 1.6 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT PRIMARY KEY,
    dict_name VARCHAR(64) NOT NULL,
    dict_type VARCHAR(64) NOT NULL UNIQUE,
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag INT NOT NULL DEFAULT 0
);

-- 1.7 字典数据表 (parent_id 支持无限多级树形层级数据)
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT PRIMARY KEY,
    dict_type VARCHAR(64) NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    dict_label VARCHAR(64) NOT NULL,
    dict_value VARCHAR(64) NOT NULL,
    sort_order INT DEFAULT 0,
    list_class VARCHAR(64),
    css_class VARCHAR(64),
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag INT NOT NULL DEFAULT 0
);

-- 1.8 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    ip_address VARCHAR(64),
    location VARCHAR(128),
    browser VARCHAR(64),
    os VARCHAR(64),
    status INT NOT NULL DEFAULT 1,
    msg VARCHAR(255),
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag INT NOT NULL DEFAULT 0
);

-- 1.9 操作日志表
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id BIGINT PRIMARY KEY,
    title VARCHAR(64),
    business_type INT DEFAULT 0, -- 0-其它 1-新增 2-修改 3-删除 4-授权 5-导出
    method VARCHAR(128),
    request_method VARCHAR(10),
    oper_name VARCHAR(64),
    oper_url VARCHAR(255),
    oper_ip VARCHAR(64),
    oper_location VARCHAR(128),
    oper_param TEXT,
    json_result TEXT,
    status INT NOT NULL DEFAULT 1, -- 1-成功 0-失败
    error_msg VARCHAR(2000),
    oper_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    del_flag INT NOT NULL DEFAULT 0
);

-- 1.10 OAuth 2.1 客户端持久化存储表
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id VARCHAR(100) NOT NULL PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL UNIQUE,
    client_id_issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at TIMESTAMP DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL
);

-- =================================================================
-- 2. 预置初始化种子数据 (Seeds - 角色与关联架构；admin 用户账号由应用启动动态生成)
-- =================================================================

-- 2.1 初始化超级管理员角色
INSERT INTO sys_role (id, role_code, role_name, status, create_by)
VALUES (1, 'ROLE_ADMIN', '超级管理员', 1, 1)
ON CONFLICT (id) DO NOTHING;

-- 2.3 绑定 admin 到 ROLE_ADMIN
INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1)
ON CONFLICT DO NOTHING;

-- 2.4 初始化系统菜单与按钮节点 (包含全量 27 个目录、菜单与按钮节点)
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, permission, icon, sort_order, menu_type, create_by)
VALUES 
-- 1. 目录节点
(1, 0, '系统管理', '/system', 'Layout', NULL, 'setting', 1, 0, 1),

-- 2. 菜单节点
(2, 1, '用户管理', '/system/user', 'system/user/index', 'sys:user:list', 'user', 1, 1, 1),
(3, 1, '菜单管理', '/system/menu', 'system/menu/index', 'sys:menu:list', 'menu', 2, 1, 1),
(9, 1, '角色权限管理', '/system/role', 'system/role/index', 'sys:role:list', 'key', 3, 1, 1),
(4, 1, '登录日志', '/system/log', 'system/log/index', 'sys:log:list', 'document', 4, 1, 1),
(8, 1, '字典管理', '/system/dict', 'system/dict/index', 'sys:dict:list', 'notebook', 5, 1, 1),
(10, 1, '操作日志', '/system/log/oper', 'system/log/oper/index', 'sys:log:oper', 'tickets', 6, 1, 1),

-- 3. 用户管理 (id=2) 按钮节点
(5, 2, '新增用户', NULL, NULL, 'sys:user:add', NULL, 1, 2, 1),
(6, 2, '修改用户', NULL, NULL, 'sys:user:edit', NULL, 2, 2, 1),
(7, 2, '重置密码', NULL, NULL, 'sys:user:reset', NULL, 3, 2, 1),
(11, 2, '删除用户', NULL, NULL, 'sys:user:delete', NULL, 4, 2, 1),
(12, 2, '导出用户', NULL, NULL, 'sys:user:export', NULL, 5, 2, 1),

-- 4. 菜单管理 (id=3) 按钮节点
(30, 3, '新增菜单', NULL, NULL, 'sys:menu:add', NULL, 1, 2, 1),
(31, 3, '修改菜单', NULL, NULL, 'sys:menu:edit', NULL, 2, 2, 1),
(32, 3, '删除菜单', NULL, NULL, 'sys:menu:delete', NULL, 3, 2, 1),

-- 5. 角色权限管理 (id=9) 按钮节点
(20, 9, '新增角色', NULL, NULL, 'sys:role:add', NULL, 1, 2, 1),
(21, 9, '修改角色', NULL, NULL, 'sys:role:edit', NULL, 2, 2, 1),
(22, 9, '删除角色', NULL, NULL, 'sys:role:delete', NULL, 3, 2, 1),
(23, 9, '角色授权', NULL, NULL, 'sys:role:authorize', NULL, 4, 2, 1),

-- 6. 字典管理 (id=8) 按钮节点
(40, 8, '新增字典类型', NULL, NULL, 'sys:dict:type:add', NULL, 1, 2, 1),
(41, 8, '修改字典类型', NULL, NULL, 'sys:dict:type:edit', NULL, 2, 2, 1),
(42, 8, '删除字典类型', NULL, NULL, 'sys:dict:type:delete', NULL, 3, 2, 1),
(43, 8, '新增字典数据', NULL, NULL, 'sys:dict:data:add', NULL, 4, 2, 1),
(44, 8, '修改字典数据', NULL, NULL, 'sys:dict:data:edit', NULL, 5, 2, 1),
(45, 8, '删除字典数据', NULL, NULL, 'sys:dict:data:delete', NULL, 6, 2, 1),

-- 7. 操作日志 (id=10) 按钮节点
(50, 10, '删除操作日志', NULL, NULL, 'sys:log:oper:delete', NULL, 1, 2, 1),
(51, 10, '清空操作日志', NULL, NULL, 'sys:log:oper:clean', NULL, 2, 2, 1)

ON CONFLICT (id) DO NOTHING;

-- 2.5 绑定 ROLE_ADMIN 所有菜单与按钮权限 (动态全量授权)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_code = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- 2.6 预置字典类型种子数据
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, remark, create_by)
VALUES 
(1, '通用状态', 'sys_common_status', 1, '系统通用启用/禁用状态', 1),
(2, '登录状态', 'sys_login_status', 1, '登录日志成功/失败状态', 1),
(3, '用户性别', 'sys_user_sex', 1, '用户性别代码', 1),
(4, '行政区划', 'sys_region', 1, '省市区多级行政区划示例', 1)
ON CONFLICT (id) DO NOTHING;

-- 2.7 预置字典数据种子数据 (包含多级树形结构)
INSERT INTO sys_dict_data (id, dict_type, parent_id, dict_label, dict_value, sort_order, list_class, status, create_by)
VALUES
(1, 'sys_common_status', 0, '正常', '1', 1, 'success', 1, 1),
(2, 'sys_common_status', 0, '禁用', '0', 2, 'danger', 1, 1),
(3, 'sys_login_status', 0, '成功', '1', 1, 'success', 1, 1),
(4, 'sys_login_status', 0, '失败', '0', 2, 'danger', 1, 1),
(5, 'sys_user_sex', 0, '男', '1', 1, 'primary', 1, 1),
(6, 'sys_user_sex', 0, '女', '2', 2, 'danger', 1, 1),
(7, 'sys_user_sex', 0, '未知', '0', 3, 'info', 1, 1),
(100, 'sys_region', 0, '广东省', '440000', 1, 'primary', 1, 1),
(101, 'sys_region', 100, '深圳市', '440300', 1, 'success', 1, 1),
(102, 'sys_region', 100, '广州市', '440100', 2, 'success', 1, 1),
(103, 'sys_region', 101, '南山区', '440305', 1, 'info', 1, 1),
(104, 'sys_region', 101, '福田区', '440304', 2, 'info', 1, 1)
ON CONFLICT (id) DO NOTHING;
