-- GitLab 提交表
CREATE TABLE IF NOT EXISTS gitlab_commit
(
    id                  VARCHAR(32)  PRIMARY KEY,
    project_id          INTEGER      NOT NULL,
    branch_id           VARCHAR(32)  NOT NULL,
    commit_id           VARCHAR(100) NOT NULL,
    title               VARCHAR(500),
    message             TEXT,
    committer_name      VARCHAR(100),
    committer_email     VARCHAR(200),
    committed_date      TIMESTAMP,
    create_time         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    del_flag            SMALLINT     DEFAULT 0
);

-- 注释
COMMENT ON TABLE gitlab_commit IS 'GitLab 提交表';
COMMENT ON COLUMN gitlab_commit.id IS '主键 ID';
COMMENT ON COLUMN gitlab_commit.project_id IS 'GitLab 项目 ID';
COMMENT ON COLUMN gitlab_commit.branch_id IS '分支 ID';
COMMENT ON COLUMN gitlab_commit.commit_id IS '提交 ID';
COMMENT ON COLUMN gitlab_commit.title IS '提交标题';
COMMENT ON COLUMN gitlab_commit.message IS '提交信息';
COMMENT ON COLUMN gitlab_commit.committer_name IS '提交者姓名';
COMMENT ON COLUMN gitlab_commit.committer_email IS '提交者邮箱';
COMMENT ON COLUMN gitlab_commit.committed_date IS '提交时间';
COMMENT ON COLUMN gitlab_commit.create_time IS '创建时间';
COMMENT ON COLUMN gitlab_commit.update_time IS '更新时间';
COMMENT ON COLUMN gitlab_commit.del_flag IS '删除状态：0-有效，1-已删除';

-- 索引
CREATE INDEX IF NOT EXISTS idx_gitlab_commit_project_id ON gitlab_commit (project_id);
CREATE INDEX IF NOT EXISTS idx_gitlab_commit_branch_id ON gitlab_commit (branch_id);
CREATE INDEX IF NOT EXISTS idx_gitlab_commit_committed_date ON gitlab_commit (committed_date);
