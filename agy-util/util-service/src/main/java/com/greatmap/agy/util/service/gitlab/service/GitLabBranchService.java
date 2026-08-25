package com.greatmap.agy.util.service.gitlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.util.api.dto.gitlab.GitLabBranchDTO;
import com.greatmap.agy.util.service.gitlab.entity.GitLabBranch;

import java.util.List;

/**
 * GitLab 分支持久层服务接口
 *
 * @author AGY
 */
public interface GitLabBranchService extends IService<GitLabBranch> {

    /**
     * 同步活跃项目的分支及提交历史
     */
    int syncActiveProjectBranches();

    /**
     * 查询本地数据库已同步的所有分支列表
     */
    List<GitLabBranchDTO> listAllBranches();

    /**
     * 根据项目 ID 查询分支列表
     */
    List<GitLabBranchDTO> getBranchesByProjectId(Integer projectId);
}
