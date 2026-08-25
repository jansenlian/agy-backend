package com.greatmap.agy.util.api.dto.gitlab;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * GitLab 提交 DTO
 *
 * @author AGY
 */
@Data
@Schema(description = "GitLab 提交记录数据传输对象")
public class GitLabCommitDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "GitLab 项目 ID")
    private Integer projectId;

    @Schema(description = "分支 ID")
    private String branchId;

    @Schema(description = "分支名称")
    private String branchName;

    @Schema(description = "提交 Hash ID")
    private String commitId;

    @Schema(description = "提交标题")
    private String title;

    @Schema(description = "提交信息完整内容")
    private String message;

    @Schema(description = "提交者姓名")
    private String committerName;

    @Schema(description = "提交者邮箱")
    private String committerEmail;

    @Schema(description = "提交时间")
    private String committedDate;

    @Schema(description = "本地入库创建时间")
    private String createTime;
}
