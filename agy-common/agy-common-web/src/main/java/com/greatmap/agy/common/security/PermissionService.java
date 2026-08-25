package com.greatmap.agy.common.security;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.greatmap.agy.common.constant.AuthConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义 RBAC 权限与角色验证表达式服务 (注册为 Spring Bean 名称: ss)
 * 支持在 Controller 方法上使用:
 * - @PreAuthorize("@ss.hasPerm('sys:user:add')")
 * - @PreAuthorize("@ss.hasAnyPerms('sys:user:add', 'sys:user:edit')")
 * - @PreAuthorize("@ss.hasRole('admin')")
 * - @PreAuthorize("@ss.hasAnyRoles('admin', 'manager')")
 */
@Slf4j
@Component("ss")
public class PermissionService {

    /**
     * 验证当前登录用户是否拥有指定权限 (支持超级管理员 *:*:* 特权通行)
     *
     * @param permission 权限标识 (例如 "sys:user:delete")
     * @return 是否拥有权限
     */
    public boolean hasPerm(String permission) {
        if (StrUtil.isBlank(permission)) {
            return false;
        }

        Set<String> authorities = getCurrentAuthorities();
        if (CollUtil.isEmpty(authorities)) {
            return false;
        }

        // 1. 超级管理员拥有 *:*:* 全局通配符特权或 ROLE_ADMIN 角色，直接放行
        if (authorities.contains(AuthConstants.SUPER_ADMIN_PERMISSION) || authorities.contains("ROLE_ADMIN")) {
            return true;
        }

        // 2. 精确匹配权限标识
        return authorities.contains(permission.trim());
    }

    /**
     * hasPerm 的友好全称别名
     */
    public boolean hasPermission(String permission) {
        return hasPerm(permission);
    }

    /**
     * 验证当前登录用户是否拥有任意一个指定权限 (OR 逻辑)
     *
     * @param permissions 权限标识数组
     * @return 是否拥有任一权限
     */
    public boolean hasAnyPerms(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }

        Set<String> authorities = getCurrentAuthorities();
        if (CollUtil.isEmpty(authorities)) {
            return false;
        }

        if (authorities.contains(AuthConstants.SUPER_ADMIN_PERMISSION) || authorities.contains("ROLE_ADMIN")) {
            return true;
        }

        for (String perm : permissions) {
            if (StrUtil.isNotBlank(perm) && authorities.contains(perm.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * hasAnyPerms 的友好全称别名
     */
    public boolean hasAnyPermissions(String... permissions) {
        return hasAnyPerms(permissions);
    }

    /**
     * 验证当前登录用户是否拥有指定角色
     *
     * @param role 角色标识 (例如 "admin")
     * @return 是否拥有角色
     */
    public boolean hasRole(String role) {
        if (StrUtil.isBlank(role)) {
            return false;
        }

        Set<String> authorities = getCurrentAuthorities();
        if (CollUtil.isEmpty(authorities)) {
            return false;
        }

        if (authorities.contains(AuthConstants.SUPER_ADMIN_PERMISSION)) {
            return true;
        }

        String roleKey = role.trim();
        return authorities.contains(roleKey) || authorities.contains("ROLE_" + roleKey);
    }

    /**
     * 验证当前登录用户是否拥有任意一个指定角色 (OR 逻辑)
     *
     * @param roles 角色标识数组
     * @return 是否拥有任一角色
     */
    public boolean hasAnyRoles(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }

        Set<String> authorities = getCurrentAuthorities();
        if (CollUtil.isEmpty(authorities)) {
            return false;
        }

        if (authorities.contains(AuthConstants.SUPER_ADMIN_PERMISSION)) {
            return true;
        }

        for (String role : roles) {
            if (StrUtil.isNotBlank(role)) {
                String roleKey = role.trim();
                if (authorities.contains(roleKey) || authorities.contains("ROLE_" + roleKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从当前 Spring Security 上下文中提取用户权限集合
     */
    private Set<String> getCurrentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return Set.of();
        }

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
