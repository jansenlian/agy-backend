package com.greatmap.agy.util.service.gitlab.service;

import com.greatmap.agy.util.api.dto.gitlab.GitLabProjectStats;

import java.util.List;

/**
 * GitLab 业务综合服务接口
 *
 * @author AGY
 */
public interface GitLabService {

    /**
     * 获取当前用户的所有项目及其分支代码变更统计
     */
    List<GitLabProjectStats> getAllUserProjectsWithBranches();

    /**
     * 获取指定项目的所有分支代码变更统计
     *
     * @param projectIdOrPath 项目 ID 或项目路径
     */
    GitLabProjectStats getProjectBranchesStats(String projectIdOrPath);
}
