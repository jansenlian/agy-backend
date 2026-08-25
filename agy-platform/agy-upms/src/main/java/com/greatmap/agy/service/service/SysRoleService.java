package com.greatmap.agy.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.api.dto.SysRoleSaveDTO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysRoleVO;
import com.greatmap.agy.service.entity.SysRole;

import java.util.List;

/**
 * 角色 Service
 */
public interface SysRoleService extends IService<SysRole> {

    PageResultVO<SysRoleVO> getRolePage(int pageNo, int pageSize, String roleName, String roleCode);

    List<SysRoleVO> getAllRoles();

    SysRoleVO getRoleVOById(Long id);

    List<Long> getRoleMenuIds(Long roleId);

    boolean saveRoleMenus(Long roleId, List<Long> menuIds);

    boolean saveRole(SysRoleSaveDTO dto);

    boolean updateRole(SysRoleSaveDTO dto);

    /**
     * 安全删除角色 (内置超级管理员角色禁止删除，并级联清理角色菜单关联)
     */
    boolean deleteRole(Long roleId);
}
