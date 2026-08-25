package com.greatmap.agy.util.service.gitlab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.util.service.gitlab.entity.GitLabProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * GitLab 项目 Mapper
 *
 * @author AGY
 */
@Mapper
public interface GitLabProjectMapper extends BaseMapper<GitLabProject> {
}
