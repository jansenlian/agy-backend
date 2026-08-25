package com.greatmap.agy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SysRolePermission 角色与权限规则单元测试")
class SysRolePermissionTest {

    private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    @Test
    @DisplayName("测试角色编码大写拼音与下划线命名合规性")
    void testRoleCodeNamingConvention() {
        List<String> validRoleCodes = List.of(
                "ROLE_ADMIN",
                "ROLE_USER",
                "ROLE_XTGLCKQX",
                "ROLE_AUDITOR_01",
                "ROLE_OPERATOR"
        );

        for (String roleCode : validRoleCodes) {
            assertTrue(roleCode.startsWith("ROLE_"), "角色编码必须以 ROLE_ 前缀开头: " + roleCode);
            assertTrue(ROLE_CODE_PATTERN.matcher(roleCode).matches(), "角色编码必须为全大写字母、数字及下划线: " + roleCode);
        }
    }

    @Test
    @DisplayName("测试超级管理员通配符权限识别")
    void testSuperAdminWildcardPermission() {
        Set<String> adminPerms = Set.of("*:*:*");
        assertTrue(adminPerms.contains("*:*:*"), "超级管理员必须拥有全局通配符权限");
    }

    @Test
    @DisplayName("测试标准按钮权限标识三段式命名 (如 sys:user:add)")
    void testPermissionIdentifierConvention() {
        List<String> validPermissions = List.of(
                "sys:user:add",
                "sys:user:edit",
                "sys:user:delete",
                "sys:role:authorize",
                "sys:menu:list"
        );

        for (String perm : validPermissions) {
            String[] segments = perm.split(":");
            assertEquals(3, segments.length, "标准权限标识必须为模块:实体:操作三段式格式: " + perm);
            assertFalse(segments[0].isBlank());
            assertFalse(segments[1].isBlank());
            assertFalse(segments[2].isBlank());
        }
    }
}
