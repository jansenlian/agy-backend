package com.greatmap.agy.util.service.gitlab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.util.service.gitlab.entity.GitLabCommit;
import org.apache.ibatis.annotations.Mapper;

/**
 * GitLab 提交 Mapper
 *
 * @author AGY
 */
@Mapper
public interface GitLabCommitMapper extends BaseMapper<GitLabCommit> {
}
