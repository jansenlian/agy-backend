package com.greatmap.agy.util.api.dto.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * GitLab 项目 DTO
 *
 * @author AGY
 */
@Data
@Schema(description = "GitLab 项目数据传输对象")
public class GitLabProjectDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "GitLab 远程项目 ID")
    private Integer projectId;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "项目名称")
    private String name;

    @Schema(description = "项目路径（带命名空间）")
    private String pathWithNamespace;

    @Schema(description = "默认分支")
    private String defaultBranch;

    @Schema(description = "HTTP 仓库地址")
    private String httpUrlToRepo;

    @Schema(description = "GitLab 创建时间")
    private String createdAt;

    @Schema(description = "最后活动时间")
    private String lastActivityAt;

    @Schema(description = "是否活跃：0-不活跃，1-活跃")
    private Integer isActive;

    @Schema(description = "本地入库创建时间")
    private String createTime;

    @Schema(description = "本地入库更新时间")
    private String updateTime;
}
