package com.greatmap.agy.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.hr.entity.ServerEnvironment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServerEnvironmentMapper extends BaseMapper<ServerEnvironment> {

    /**
     * 根据项目名称或 IP 查询测试环境服务器
     */
    List<ServerEnvironment> findByProjectOrIp(@Param("projectName") String projectName);
}
