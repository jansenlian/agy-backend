package com.greatmap.agy.util.api.dto.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * GitLab 分支统计信息
 *
 * @author AGY
 */
@Data
@Schema(description = "GitLab 分支统计信息")
public class GitLabBranchStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分支名称")
    private String branchName;

    @Schema(description = "最后提交时间")
    private String lastCommitTime;

    @Schema(description = "提交明细列表")
    private List<GitLabCommitStats> commits;

    @Schema(description = "分支总增加代码行数")
    private Integer totalAdditions;

    @Schema(description = "分支总删除代码行数")
    private Integer totalDeletions;

    @Schema(description = "分支总修改代码行数")
    private Integer totalChanges;
}
