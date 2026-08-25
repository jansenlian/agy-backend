package com.greatmap.agy.util.api.dto.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * GitLab 项目全景统计信息
 *
 * @author AGY
 */
@Data
@Schema(description = "GitLab 项目全景统计信息")
public class GitLabProjectStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目 ID")
    private Integer projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目路径")
    private String projectPath;

    @Schema(description = "分支统计列表")
    private List<GitLabBranchStats> branches;

    @Schema(description = "项目总增加代码行数")
    private Integer totalAdditions;

    @Schema(description = "项目总删除代码行数")
    private Integer totalDeletions;

    @Schema(description = "项目总修改代码行数")
    private Integer totalChanges;
}
