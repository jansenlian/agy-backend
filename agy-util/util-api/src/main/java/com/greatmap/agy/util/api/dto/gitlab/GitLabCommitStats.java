package com.greatmap.agy.util.api.dto.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * GitLab 提交统计信息
 *
 * @author AGY
 */
@Data
@Schema(description = "GitLab 提交统计信息")
public class GitLabCommitStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "提交 ID")
    private String id;

    @Schema(description = "提交信息")
    private String message;

    @Schema(description = "作者姓名")
    private String authorName;

    @Schema(description = "提交时间")
    private String committedDate;

    @Schema(description = "增加代码行数")
    private Integer additions;

    @Schema(description = "删除代码行数")
    private Integer deletions;

    @Schema(description = "总修改代码行数")
    private Integer totalChanges;
}
