package com.greatmap.agy.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.service.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询已授权角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询已授权角色名称列表
     */
    List<String> selectRoleNamesByUserId(@Param("userId") Long userId);

    /**
     * 批量根据用户ID集合查询用户角色名称映射
     */
    List<com.greatmap.agy.api.dto.SysUserRoleDTO> selectRoleNamesByUserIds(@Param("userIds") List<Long> userIds);
}
