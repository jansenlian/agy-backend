package com.greatmap.agy.service;

import com.greatmap.agy.api.vo.SysMenuVO;
import com.greatmap.agy.service.entity.SysMenu;
import com.greatmap.agy.service.mapper.SysMenuMapper;
import com.greatmap.agy.service.mapper.SysRoleMapper;
import com.greatmap.agy.service.service.impl.SysMenuServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SysMenuService 菜单与权限真实业务实现类单元测试")
class SysMenuServiceTest {

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @InjectMocks
    private SysMenuServiceImpl sysMenuService;

    @Test
    @DisplayName("测试真实 getUserMenuTree 方法递归构建用户菜单层级（目录 -> 菜单 -> 按钮）")
    void testGetUserMenuTree() {
        Long userId = 1002L;
        when(sysRoleMapper.selectRoleCodesByUserId(userId)).thenReturn(List.of("ROLE_USER"));

        SysMenu root = new SysMenu();
        root.setId(10L);
        root.setParentId(0L);
        root.setMenuName("系统管理");
        root.setSortOrder(1);

        SysMenu userMenu = new SysMenu();
        userMenu.setId(20L);
        userMenu.setParentId(10L);
        userMenu.setMenuName("用户管理");
        userMenu.setSortOrder(1);

        SysMenu btnAdd = new SysMenu();
        btnAdd.setId(30L);
        btnAdd.setParentId(20L);
        btnAdd.setMenuName("新增用户");
        btnAdd.setPermission("sys:user:add");

        when(sysMenuMapper.selectMenuListByUserId(userId)).thenReturn(List.of(root, userMenu, btnAdd));

        // 调用真实 Service 实现类的方法
        List<SysMenuVO> tree = sysMenuService.getUserMenuTree(userId);

        assertNotNull(tree);
        assertEquals(1, tree.size(), "顶级节点数量应为 1");
        assertEquals("系统管理", tree.get(0).getMenuName());
        assertEquals(1, tree.get(0).getChildren().size(), "系统管理应有 1 个子菜单");
        assertEquals("用户管理", tree.get(0).getChildren().get(0).getMenuName());
        assertEquals(1, tree.get(0).getChildren().get(0).getChildren().size(), "用户管理应有 1 个按钮");
        assertEquals("sys:user:add", tree.get(0).getChildren().get(0).getChildren().get(0).getPermission());
    }

    @Test
    @DisplayName("测试真实 getUserPermissions 方法提取普通用户的权限标识列表")
    void testGetUserPermissionsForRegularUser() {
        Long userId = 1002L;
        when(sysRoleMapper.selectRoleCodesByUserId(userId)).thenReturn(List.of("ROLE_USER"));
        when(sysMenuMapper.selectPermissionsByUserId(userId)).thenReturn(List.of("sys:user:list", "sys:user:add"));

        List<String> perms = sysMenuService.getUserPermissions(userId);

        assertEquals(2, perms.size());
        assertTrue(perms.contains("sys:user:list"));
        assertTrue(perms.contains("sys:user:add"));
    }
}
