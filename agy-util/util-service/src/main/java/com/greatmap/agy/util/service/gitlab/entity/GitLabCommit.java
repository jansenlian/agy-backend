package com.greatmap.agy.util.service.gitlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * GitLab 提交记录持久化实体
 *
 * @author AGY
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gitlab_commit")
public class GitLabCommit extends BaseEntity {

    /**
     * 主键 ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * GitLab 项目 ID
     */
    private Integer projectId;

    /**
     * 分支 ID（关联 gitlab_branch 的 id）
     */
    private String branchId;

    /**
     * 提交 Hash ID
     */
    private String commitId;

    /**
     * 提交标题
     */
    private String title;

    /**
     * 提交信息
     */
    private String message;

    /**
     * 提交者姓名
     */
    private String committerName;

    /**
     * 提交者邮箱
     */
    private String committerEmail;

    /**
     * 提交时间
     */
    private LocalDateTime committedDate;
}
