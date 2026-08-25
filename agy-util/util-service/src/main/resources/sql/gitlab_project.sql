-- GitLab 项目表
-- 数据库：gaigaidb

-- 如果已存在旧表，执行以下 SQL 修改：
-- 1. 修改 id 字段类型为 VARCHAR(32)
-- ALTER TABLE gitlab_project ALTER COLUMN id TYPE VARCHAR(32);
-- 2. 修改 creat_time 字段名为 create_time
-- ALTER TABLE gitlab_project RENAME COLUMN creat_time TO create_time;

CREATE TABLE IF NOT EXISTS gitlab_project
(
    id                  VARCHAR(32)  PRIMARY KEY,
    project_id          INTEGER      NOT NULL UNIQUE,
    description         TEXT,
    name                VARCHAR(255) NOT NULL,
    path_with_namespace VARCHAR(500) NOT NULL,
    default_branch      VARCHAR(100),
    create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag            SMALLINT  DEFAULT 0,
    created_at          TIMESTAMP,
    http_url_to_repo    VARCHAR(500),
    last_activity_at    TIMESTAMP,
    is_active           SMALLINT DEFAULT 0
);

-- 注释
COMMENT ON TABLE gitlab_project IS 'GitLab 项目表';
COMMENT ON COLUMN gitlab_project.id IS '主键 ID (32 位 UUID)';
COMMENT ON COLUMN gitlab_project.project_id IS 'GitLab 项目 ID';
COMMENT ON COLUMN gitlab_project.description IS '项目描述';
COMMENT ON COLUMN gitlab_project.name IS '项目名称';
COMMENT ON COLUMN gitlab_project.path_with_namespace IS '项目路径（带命名空间）';
COMMENT ON COLUMN gitlab_project.default_branch IS '默认分支';
COMMENT ON COLUMN gitlab_project.create_time IS '创建时间（本地）';
COMMENT ON COLUMN gitlab_project.update_time IS '更新时间（本地）';
COMMENT ON COLUMN gitlab_project.del_flag IS '删除状态：0-有效，1-已删除';
COMMENT ON COLUMN gitlab_project.created_at IS '项目创建时间（GitLab）';
COMMENT ON COLUMN gitlab_project.http_url_to_repo IS 'HTTP 仓库地址';
COMMENT ON COLUMN gitlab_project.last_activity_at IS '最后活动时间';
COMMENT ON COLUMN gitlab_project.is_active IS '是否活跃：0-不活跃，1-活跃';

-- 索引
CREATE INDEX IF NOT EXISTS idx_gitlab_project_name ON gitlab_project (name);
CREATE INDEX IF NOT EXISTS idx_gitlab_project_path ON gitlab_project (path_with_namespace);
CREATE INDEX IF NOT EXISTS idx_gitlab_project_create_time ON gitlab_project (create_time);
