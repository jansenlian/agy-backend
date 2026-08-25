package com.greatmap.agy.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.service.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询已授权菜单列表
     */
    List<SysMenu> selectMenuListByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限标识列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
