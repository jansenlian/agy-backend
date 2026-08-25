package com.greatmap.agy.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.agy.api.dto.SysRoleSaveDTO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysRoleVO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.common.utils.SecurityUtils;
import com.greatmap.agy.service.entity.SysRole;
import com.greatmap.agy.service.entity.SysRoleMenu;
import com.greatmap.agy.service.mapper.SysRoleMapper;
import com.greatmap.agy.service.mapper.SysRoleMenuMapper;
import com.greatmap.agy.service.mapper.SysUserRoleMapper;
import com.greatmap.agy.service.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResultVO<SysRoleVO> getRolePage(int pageNo, int pageSize, String roleName, String roleCode) {
        Page<SysRole> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(roleName), SysRole::getRoleName, roleName)
                .like(StringUtils.isNotBlank(roleCode), SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDelFlag, 0)
                .orderByDesc(SysRole::getCreateTime);
        Page<SysRole> resultPage = this.page(page, queryWrapper);
        List<SysRoleVO> voList = BeanUtil.copyToList(resultPage.getRecords(), SysRoleVO.class);
        return PageResultVO.of(voList, resultPage.getTotal(), resultPage.getSize(), resultPage.getCurrent());
    }

    @Override
    public List<SysRoleVO> getAllRoles() {
        List<SysRole> list = this.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .eq(SysRole::getDelFlag, 0)
                .orderByAsc(SysRole::getId));
        return BeanUtil.copyToList(list, SysRoleVO.class);
    }

    @Override
    public SysRoleVO getRoleVOById(Long id) {
        SysRole role = this.getById(id);
        if (role == null) {
            return null;
        }
        return BeanUtil.copyProperties(role, SysRoleVO.class);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
        return roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRole(SysRoleSaveDTO dto) {
        // 校验编码冲突
        Long count = this.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, dto.getRoleCode())
                .eq(SysRole::getDelFlag, 0));
        if (count > 0) {
            throw new BusinessException("角色编码 [" + dto.getRoleCode() + "] 已存在");
        }
        SysRole role = BeanUtil.copyProperties(dto, SysRole.class);
        return this.save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRoleSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("更新角色 ID 不能为空");
        }
        SysRole oldRole = this.getById(dto.getId());
        if (oldRole == null) {
            throw new BusinessException("角色不存在或已被删除");
        }

        // 内置超级管理员防破坏保护
        if (dto.getId() == 1L || "ROLE_ADMIN".equalsIgnoreCase(oldRole.getRoleCode())) {
            if (!"ROLE_ADMIN".equalsIgnoreCase(dto.getRoleCode())) {
                throw new BusinessException("内置超级管理员角色编码 [ROLE_ADMIN] 禁止修改");
            }
        }

        // 编码变更是检查重名
        if (!oldRole.getRoleCode().equalsIgnoreCase(dto.getRoleCode())) {
            Long count = this.count(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, dto.getRoleCode())
                    .ne(SysRole::getId, dto.getId())
                    .eq(SysRole::getDelFlag, 0));
            if (count > 0) {
                throw new BusinessException("角色编码 [" + dto.getRoleCode() + "] 已被其他角色使用");
            }
        }

        oldRole.setRoleName(dto.getRoleName());
        oldRole.setRoleCode(dto.getRoleCode());
        boolean success = this.updateById(oldRole);
        if (success) {
            evictUsersByRoleId(dto.getId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleMenus(Long roleId, List<Long> menuIds) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 越权提权防线 (Privilege Escalation Protection)：
        // 非超级管理员分配权限时，分配的菜单点必须在操作者本人拥有的权限集合子集中
        Long currentUserId = SecurityUtils.getLoginUserId();
        if (currentUserId != null && currentUserId != 1L) {
            List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(currentUserId);
            boolean isSuperAdmin = roleCodes != null && roleCodes.contains("ROLE_ADMIN");
            if (!isSuperAdmin && menuIds != null && !menuIds.isEmpty()) {
                // 查询当前登录人所拥有的菜单 ID 集合
                List<Long> currentUserMenuIds = userRoleMapper.selectMenuIdsByUserId(currentUserId);
                Set<Long> currentUserMenuSet = new HashSet<>(currentUserMenuIds != null ? currentUserMenuIds : List.of());
                for (Long targetMenuId : menuIds) {
                    if (!currentUserMenuSet.contains(targetMenuId)) {
                        throw new BusinessException("越权保护：您无法将本人未获授权的权限点分配给其他角色");
                    }
                }
            }
        }

        // 先删除旧有的关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));

        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }

        // 🔴 核心加固：角色绑定的菜单/权限一旦改变，主动作废拥有该角色的全量用户 Token 缓存
        evictUsersByRoleId(roleId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        if (roleId == null) {
            throw new BusinessException("角色 ID 不能为空");
        }
        if (roleId == 1L) {
            throw new BusinessException("系统内置超级管理员角色禁止删除");
        }
        SysRole role = this.getById(roleId);
        if (role != null && "ROLE_ADMIN".equalsIgnoreCase(role.getRoleCode())) {
            throw new BusinessException("系统内置超级管理员角色禁止删除");
        }
        // 🔴 核心加固：级联清理角色关联前作废对应用户的 Redis Token 缓存
        evictUsersByRoleId(roleId);

        // 级联清理角色菜单关联表与用户角色分配表
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        userRoleMapper.deleteByRoleId(roleId);
        return this.removeById(roleId);
    }

    private void evictUsersByRoleId(Long roleId) {
        if (roleId == null || stringRedisTemplate == null) {
            return;
        }
        try {
            List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
            if (userIds != null && !userIds.isEmpty()) {
                for (Long uId : userIds) {
                    String userTokensKey = "user_tokens:" + uId;
                    Set<String> tokens = stringRedisTemplate.opsForSet().members(userTokensKey);
                    if (tokens != null && !tokens.isEmpty()) {
                        for (String token : tokens) {
                            stringRedisTemplate.delete(com.greatmap.agy.common.constant.AuthConstants.TOKEN_PREFIX + token);
                            stringRedisTemplate.delete(com.greatmap.agy.common.constant.AuthConstants.TOKEN_PERMS_PREFIX + token);
                        }
                    }
                    stringRedisTemplate.delete(userTokensKey);
                }
            }
        } catch (Exception e) {
            log.warn("根据角色 ID [" + roleId + "] 作废关联用户 Redis Token 缓存异常: " + e.getMessage());
        }
    }
}
