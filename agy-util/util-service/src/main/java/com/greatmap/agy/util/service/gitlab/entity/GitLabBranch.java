package com.greatmap.agy.util.service.gitlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * GitLab 分支持久化实体
 *
 * @author AGY
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gitlab_branch")
public class GitLabBranch extends BaseEntity {

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
     * 分支名称
     */
    private String name;

    /**
     * 是否默认分支：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 分支 Web 地址
     */
    private String webUrl;

    /**
     * 最后提交 ID
     */
    private String commitId;

    /**
     * 最后提交时间
     */
    private LocalDateTime committedDate;
}
