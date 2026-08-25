package com.greatmap.agy.util.service.gitlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * GitLab 项目持久化实体
 *
 * @author AGY
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gitlab_project")
public class GitLabProject extends BaseEntity {

    /**
     * 主键 ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * GitLab 远程项目 ID
     */
    private Integer projectId;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目路径（带命名空间）
     */
    private String pathWithNamespace;

    /**
     * 默认分支
     */
    private String defaultBranch;

    /**
     * 创建时间（GitLab 项目创建时间）
     */
    private LocalDateTime createdAt;

    /**
     * HTTP 仓库地址
     */
    private String httpUrlToRepo;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivityAt;

    /**
     * 是否活跃：0-不活跃，1-活跃
     */
    private Integer isActive;
}
