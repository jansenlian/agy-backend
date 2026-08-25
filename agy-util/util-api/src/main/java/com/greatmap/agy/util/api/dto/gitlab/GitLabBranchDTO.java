package com.greatmap.agy.util.api.dto.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * GitLab 分支 DTO
 *
 * @author AGY
 */
@Data
@Schema(description = "GitLab 分支数据传输对象")
public class GitLabBranchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "GitLab 项目 ID")
    private Integer projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "分支名称")
    private String name;

    @Schema(description = "是否默认分支：0-否，1-是")
    private Integer isDefault;

    @Schema(description = "分支 Web 地址")
    private String webUrl;

    @Schema(description = "最后提交 ID")
    private String commitId;

    @Schema(description = "最后提交时间")
    private String committedDate;

    @Schema(description = "本地入库创建时间")
    private String createTime;

    @Schema(description = "本地入库更新时间")
    private String updateTime;
}
