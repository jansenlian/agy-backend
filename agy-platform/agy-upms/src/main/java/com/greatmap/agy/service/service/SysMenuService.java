package com.greatmap.agy.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.api.vo.SysMenuVO;
import com.greatmap.agy.service.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户 ID 动态获取授权树形菜单列表
     */
    List<SysMenuVO> getUserMenuTree(Long userId);

    /**
     * 根据用户 ID 获取其拥有的所有按钮权限标识字符串集合 (如 ['sys:user:add', 'sys:user:edit'])
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取全量系统菜单与按钮树形节点 (用于菜单管理与权限授权树)
     */
    List<SysMenuVO> getAllMenuTree();

    boolean saveMenu(com.greatmap.agy.api.dto.SysMenuSaveDTO dto);

    boolean updateMenu(com.greatmap.agy.api.dto.SysMenuSaveDTO dto);

    boolean deleteMenu(Long menuId);
}
