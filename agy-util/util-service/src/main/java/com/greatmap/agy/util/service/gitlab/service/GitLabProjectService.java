package com.greatmap.agy.util.service.gitlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.util.api.dto.gitlab.GitLabProjectDTO;
import com.greatmap.agy.util.service.gitlab.entity.GitLabProject;

import java.util.List;

/**
 * GitLab 项目持久层服务接口
 *
 * @author AGY
 */
public interface GitLabProjectService extends IService<GitLabProject> {

    /**
     * 从远程 GitLab API 查询所有项目
     */
    List<GitLabProjectDTO> getAllProjects();

    /**
     * 从远程 GitLab 同步所有项目入库
     */
    int syncAllProjects();

    /**
     * 根据 GitLab 项目 ID 查询项目详情
     */
    GitLabProjectDTO getProjectById(Integer projectId);

    /**
     * 查询本地数据库已同步的所有项目列表
     */
    List<GitLabProjectDTO> listAllProjects();
}
