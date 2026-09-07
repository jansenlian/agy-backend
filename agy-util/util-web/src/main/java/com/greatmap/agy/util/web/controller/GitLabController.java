package com.greatmap.agy.util.web.controller;

import com.greatmap.agy.util.api.dto.gitlab.GitLabBranchDTO;
import com.greatmap.agy.util.api.dto.gitlab.GitLabCommitDTO;
import com.greatmap.agy.util.api.dto.gitlab.GitLabProjectDTO;
import com.greatmap.agy.util.api.dto.gitlab.GitLabProjectStats;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.util.service.gitlab.service.GitLabBranchService;
import com.greatmap.agy.util.service.gitlab.service.GitLabCommitService;
import com.greatmap.agy.util.service.gitlab.service.GitLabProjectService;
import com.greatmap.agy.util.service.gitlab.service.GitLabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GitLab 资产、分支与提交代码统计控制器
 * 仅限超级管理员或 admin 角色调用
 *
 * @author AGY
 */
@PreAuthorize("@ss.hasRole('admin')")
@RestController
@RequestMapping("/gitlab")
@Tag(name = "GitLab资产与统计", description = "GitLab 资产同步、分支管理与提交代码量统计接口")
@RequiredArgsConstructor
public class GitLabController {

    private final GitLabService gitLabService;
    private final GitLabProjectService gitLabProjectService;
    private final GitLabBranchService gitLabBranchService;
    private final GitLabCommitService gitLabCommitService;

    @Operation(summary = "获取当前用户所有项目的分支代码统计")
    @GetMapping("/stats/all")
    public Result<List<GitLabProjectStats>> getAllUserProjectsWithBranches() {
        return Result.success(gitLabService.getAllUserProjectsWithBranches());
    }

    @Operation(summary = "获取指定项目的分支代码统计")
    @GetMapping("/stats/project")
    public Result<GitLabProjectStats> getProjectBranchesStats(
            @Parameter(description = "GitLab 项目 ID 或路径 (例如 123 或 group/repo)")
            @RequestParam String projectIdOrPath) {
        return Result.success(gitLabService.getProjectBranchesStats(projectIdOrPath));
    }

    @Operation(summary = "同步 GitLab 远程项目入库")
    @PostMapping("/project/sync")
    public Result<Integer> syncProjects() {
        return Result.success(gitLabProjectService.syncAllProjects());
    }

    @Operation(summary = "查询本地数据库已同步的项目列表")
    @GetMapping("/project/list")
    public Result<List<GitLabProjectDTO>> listProjects() {
        return Result.success(gitLabProjectService.listAllProjects());
    }

    @Operation(summary = "根据项目 ID 查询项目详情")
    @GetMapping("/project/{projectId}")
    public Result<GitLabProjectDTO> getProjectById(@PathVariable Integer projectId) {
        return Result.success(gitLabProjectService.getProjectById(projectId));
    }

    @Operation(summary = "同步所有活跃项目的分支与提交历史入库")
    @PostMapping("/branch/sync")
    public Result<Integer> syncBranches() {
        return Result.success(gitLabBranchService.syncActiveProjectBranches());
    }

    @Operation(summary = "查询本地数据库已同步的分支列表")
    @GetMapping("/branch/list")
    public Result<List<GitLabBranchDTO>> listBranches() {
        return Result.success(gitLabBranchService.listAllBranches());
    }

    @Operation(summary = "根据项目 ID 查询所有分支")
    @GetMapping("/branch/project/{projectId}")
    public Result<List<GitLabBranchDTO>> getBranchesByProjectId(@PathVariable Integer projectId) {
        return Result.success(gitLabBranchService.getBranchesByProjectId(projectId));
    }

    @Operation(summary = "根据分支 ID 查询提交记录历史")
    @GetMapping("/commit/branch/{branchId}")
    public Result<List<GitLabCommitDTO>> getCommitsByBranchId(@PathVariable String branchId) {
        return Result.success(gitLabCommitService.getCommitsByBranchId(branchId));
    }
}
