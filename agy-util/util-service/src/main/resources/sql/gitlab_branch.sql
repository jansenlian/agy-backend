-- GitLab 分支表
CREATE TABLE IF NOT EXISTS gitlab_branch
(
    id                  VARCHAR(32)  PRIMARY KEY,
    project_id          INTEGER      NOT NULL,
    name                VARCHAR(255) NOT NULL,
    is_default          SMALLINT     DEFAULT 0,
    web_url             VARCHAR(500),
    commit_id           VARCHAR(100),
    committed_date      TIMESTAMP,
    create_time         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    del_flag            SMALLINT     DEFAULT 0
);

-- 注释
COMMENT ON TABLE gitlab_branch IS 'GitLab 分支表';
COMMENT ON COLUMN gitlab_branch.id IS '主键 ID';
COMMENT ON COLUMN gitlab_branch.project_id IS 'GitLab 项目 ID';
COMMENT ON COLUMN gitlab_branch.name IS '分支名称';
COMMENT ON COLUMN gitlab_branch.is_default IS '是否默认分支：0-否，1-是';
COMMENT ON COLUMN gitlab_branch.web_url IS '分支 Web 地址';
COMMENT ON COLUMN gitlab_branch.commit_id IS '最后提交 ID';
COMMENT ON COLUMN gitlab_branch.committed_date IS '最后提交时间';
COMMENT ON COLUMN gitlab_branch.create_time IS '创建时间';
COMMENT ON COLUMN gitlab_branch.update_time IS '更新时间';
COMMENT ON COLUMN gitlab_branch.del_flag IS '删除状态：0-有效，1-已删除';

-- 索引
CREATE INDEX IF NOT EXISTS idx_gitlab_branch_project_id ON gitlab_branch (project_id);
CREATE INDEX IF NOT EXISTS idx_gitlab_branch_name ON gitlab_branch (name);
CREATE INDEX IF NOT EXISTS idx_gitlab_branch_committed_date ON gitlab_branch (committed_date);
