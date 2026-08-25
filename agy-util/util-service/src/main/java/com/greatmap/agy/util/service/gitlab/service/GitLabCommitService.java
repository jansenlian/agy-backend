package com.greatmap.agy.util.service.gitlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.util.api.dto.gitlab.GitLabCommitDTO;
import com.greatmap.agy.util.service.gitlab.entity.GitLabCommit;

import java.util.List;

/**
 * GitLab 提交记录持久层服务接口
 *
 * @author AGY
 */
public interface GitLabCommitService extends IService<GitLabCommit> {

    /**
     * 同步指定分支的提交记录入库
     */
    int syncCommits(String branchId, Integer projectId);

    /**
     * 根据分支 ID 查询提交历史
     */
    List<GitLabCommitDTO> getCommitsByBranchId(String branchId);
}
