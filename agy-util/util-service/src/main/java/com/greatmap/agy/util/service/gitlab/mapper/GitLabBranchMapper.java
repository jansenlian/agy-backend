package com.greatmap.agy.util.service.gitlab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.util.service.gitlab.entity.GitLabBranch;
import org.apache.ibatis.annotations.Mapper;

/**
 * GitLab 分支 Mapper
 *
 * @author AGY
 */
@Mapper
public interface GitLabBranchMapper extends BaseMapper<GitLabBranch> {
}
