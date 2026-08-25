package com.greatmap.agy.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PermissionService RBAC 自定义权限表达式单元测试")
class PermissionServiceTest {

    private final PermissionService permissionService = new PermissionService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("测试超级管理员拥有 *:*:* 通配符时，任意权限均放行")
    void testSuperAdminWildcardPermission() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin", null,
                        List.of(new SimpleGrantedAuthority("*:*:*"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        assertTrue(permissionService.hasPerm("sys:user:delete"));
        assertTrue(permissionService.hasPerm("sys:role:authorize"));
        assertTrue(permissionService.hasPerm("any:arbitrary:permission"));
        assertTrue(permissionService.hasAnyPerms("a", "b", "c"));
        assertTrue(permissionService.hasRole("admin"));
        assertTrue(permissionService.hasRole("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("测试普通用户仅拥有特定权限时的鉴权逻辑 (精确拦截未分配权限)")
    void testNormalUserSpecificPermissions() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "operator", null,
                        List.of(
                                new SimpleGrantedAuthority("sys:user:list"),
                                new SimpleGrantedAuthority("sys:user:add"),
                                new SimpleGrantedAuthority("ROLE_OPERATOR")
                        )
                )
        );

        // 已分配权限 -> 放行
        assertTrue(permissionService.hasPerm("sys:user:list"));
        assertTrue(permissionService.hasPerm("sys:user:add"));
        assertTrue(permissionService.hasAnyPerms("sys:user:delete", "sys:user:add"));

        // 未分配高危权限 -> 严格拦截
        assertFalse(permissionService.hasPerm("sys:user:delete"));
        assertFalse(permissionService.hasPerm("sys:role:authorize"));
        assertFalse(permissionService.hasAnyPerms("sys:menu:delete", "sys:dict:delete"));

        // 角色校验
        assertTrue(permissionService.hasRole("OPERATOR"));
        assertFalse(permissionService.hasRole("ADMIN"));
    }

    @Test
    @DisplayName("测试未登录匿名用户或空权限列表时的严格拒绝")
    void testUnauthenticatedUser() {
        SecurityContextHolder.clearContext();

        assertFalse(permissionService.hasPerm("sys:user:list"));
        assertFalse(permissionService.hasAnyPerms("sys:user:list"));
        assertFalse(permissionService.hasRole("admin"));
    }
}
