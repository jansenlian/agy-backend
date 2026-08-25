-- ========================================================
-- AGY 极速通用业务框架 (Business Microservice) 核心数据库表结构初始化
-- 仅存储业务领域专属表 (biz_*)，不包含任何 UPMS 系统管理表
-- ========================================================

CREATE TABLE IF NOT EXISTS biz_demo (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    del_flag INT DEFAULT 0 NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

COMMENT ON TABLE biz_demo IS '业务示范表';
COMMENT ON COLUMN biz_demo.id IS '主键 ID';
COMMENT ON COLUMN biz_demo.name IS '示范业务名称';
COMMENT ON COLUMN biz_demo.description IS '示范业务描述';
COMMENT ON COLUMN biz_demo.del_flag IS '逻辑删除标志 (0 正常, 1 已删除)';
COMMENT ON COLUMN biz_demo.create_time IS '创建时间';
COMMENT ON COLUMN biz_demo.update_time IS '更新时间';
COMMENT ON COLUMN biz_demo.create_by IS '创建人';
COMMENT ON COLUMN biz_demo.update_by IS '更新人';

-- 插入示范初始数据
INSERT INTO biz_demo (id, name, description, del_flag, create_time, update_time, create_by, update_by)
VALUES (1001, 'AGY 高性能业务工程示范项', '完全剥离 UPMS 的高内聚、干净业务工程，领域模型独立解耦', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin')
ON CONFLICT (id) DO NOTHING;
