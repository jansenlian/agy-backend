package com.greatmap.agy.service;

import com.greatmap.agy.api.dto.SysDictTypeSaveDTO;
import com.greatmap.agy.api.dto.SysMenuSaveDTO;
import com.greatmap.agy.api.dto.SysRoleSaveDTO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.service.entity.SysDictType;
import com.greatmap.agy.service.entity.SysRole;
import com.greatmap.agy.service.mapper.SysDictDataMapper;
import com.greatmap.agy.service.mapper.SysDictTypeMapper;
import com.greatmap.agy.service.mapper.SysMenuMapper;
import com.greatmap.agy.service.mapper.SysRoleMapper;
import com.greatmap.agy.service.mapper.SysRoleMenuMapper;
import com.greatmap.agy.service.mapper.SysUserMapper;
import com.greatmap.agy.service.mapper.SysUserRoleMapper;
import com.greatmap.agy.service.service.impl.SysDictTypeServiceImpl;
import com.greatmap.agy.service.service.impl.SysMenuServiceImpl;
import com.greatmap.agy.service.service.impl.SysRoleServiceImpl;
import com.greatmap.agy.service.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("权限模型业务约束与防越权提权防线单元测试")
class PrivilegeConstraintTest {

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysMenuMapper menuMapper;

    @Mock
    private SysDictTypeMapper dictTypeMapper;

    @Mock
    private SysDictDataMapper dictDataMapper;

    @Mock
    private SysUserMapper userMapper;

    @InjectMocks
    private SysRoleServiceImpl roleService;

    @InjectMocks
    private SysMenuServiceImpl menuService;

    @InjectMocks
    private SysDictTypeServiceImpl dictTypeService;

    @InjectMocks
    private SysUserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
        ReflectionTestUtils.setField(menuService, "baseMapper", menuMapper);
        ReflectionTestUtils.setField(dictTypeService, "baseMapper", dictTypeMapper);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    @DisplayName("测试修改内置超级管理员角色编码触发硬约束拒绝")
    void testUpdateSuperAdminRoleCodeFails() {
        SysRole adminRole = new SysRole();
        adminRole.setId(1L);
        adminRole.setRoleName("超级管理员");
        adminRole.setRoleCode("ROLE_ADMIN");

        when(roleMapper.selectById(1L)).thenReturn(adminRole);

        SysRoleSaveDTO dto = new SysRoleSaveDTO();
        dto.setId(1L);
        dto.setRoleName("普通篡改名称");
        dto.setRoleCode("ROLE_NORMAL");

        assertThrows(BusinessException.class, () -> roleService.updateRole(dto),
                "修改超级管理员 roleCode 必须触发 BusinessException 拒绝");
    }

    @Test
    @DisplayName("测试删除内置超级管理员角色触发硬约束拒绝")
    void testDeleteSuperAdminRoleFails() {
        lenient().when(roleMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> roleService.deleteRole(1L),
                "删除超级管理员必须触发 BusinessException 拒绝");
    }

    @Test
    @DisplayName("测试修改菜单上级为自身触发循环引用拒绝")
    void testUpdateMenuParentToSelfFails() {
        SysMenuSaveDTO dto = new SysMenuSaveDTO();
        dto.setId(10L);
        dto.setParentId(10L);
        dto.setTitle("测试菜单");
        dto.setMenuType(1);

        assertThrows(BusinessException.class, () -> menuService.updateMenu(dto),
                "菜单上级为自身必须触发 BusinessException 拒绝");
    }

    @Test
    @DisplayName("测试删除含有下级节点的菜单触发孤儿保护拒绝")
    void testDeleteMenuWithChildrenFails() {
        when(menuMapper.selectCount(any())).thenReturn(2L);

        assertThrows(BusinessException.class, () -> menuService.deleteMenu(5L),
                "存在下级节点的菜单删除必须触发 BusinessException 拒绝");
    }

    @Test
    @DisplayName("测试删除含有关联数据项的字典类型触发强校验拒绝")
    void testDeleteDictTypeWithDataFails() {
        SysDictType dictType = new SysDictType();
        dictType.setId(3L);
        dictType.setDictType("sys_user_sex");

        when(dictTypeMapper.selectById(3L)).thenReturn(dictType);
        when(dictDataMapper.selectCount(any())).thenReturn(5L);

        assertThrows(BusinessException.class, () -> dictTypeService.deleteDictType(3L),
                "存在字典数据的字典类型删除必须触发 BusinessException 拒绝");
    }

    @Test
    @DisplayName("测试非超级管理员给用户分配 ROLE_ADMIN 触发越权拒绝")
    void testNonSuperAdminAssignsRoleAdminFails() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "123", List.of()));

        SysRole adminRole = new SysRole();
        adminRole.setId(1L);
        adminRole.setRoleCode("ROLE_ADMIN");

        when(userRoleMapper.selectRoleCodesByUserId(any())).thenReturn(List.of("ROLE_OPERATOR"));
        when(roleMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(adminRole));

        assertThrows(BusinessException.class, () -> userService.saveUserRoles(2L, List.of(1L)),
                "普通管理员试图授予 ROLE_ADMIN 必须触发 BusinessException 拒绝");

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("测试非超级管理员试图重置超级管理员密码触发越权拒绝")
    void testNonSuperAdminResetSuperAdminPasswordFails() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "123", List.of()));

        com.greatmap.agy.service.entity.SysUser adminUser = new com.greatmap.agy.service.entity.SysUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");

        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(userRoleMapper.selectRoleCodesByUserId(2L)).thenReturn(List.of("ROLE_OPERATOR"));

        assertThrows(BusinessException.class, () -> userService.resetPassword(1L, "newPassword123"),
                "非超管重置超级管理员密码必须触发 BusinessException 越权拒绝");

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("测试非超级管理员试图修改超级管理员信息触发越权拒绝")
    void testNonSuperAdminUpdateSuperAdminUserFails() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "123", List.of()));

        com.greatmap.agy.service.entity.SysUser adminUser = new com.greatmap.agy.service.entity.SysUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");

        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(userRoleMapper.selectRoleCodesByUserId(2L)).thenReturn(List.of("ROLE_OPERATOR"));

        com.greatmap.agy.api.dto.SysUserUpdateDTO dto = new com.greatmap.agy.api.dto.SysUserUpdateDTO();
        dto.setId(1L);
        dto.setRealName("篡改姓名");

        assertThrows(BusinessException.class, () -> userService.updateUser(dto),
                "非超管修改超级管理员信息必须触发 BusinessException 越权拒绝");

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("测试任何用户试图停用超级管理员账号触发拒绝")
    void testDisableSuperAdminAccountFails() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "123", List.of()));

        com.greatmap.agy.service.entity.SysUser adminUser = new com.greatmap.agy.service.entity.SysUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");

        when(userMapper.selectById(1L)).thenReturn(adminUser);

        com.greatmap.agy.api.dto.SysUserUpdateDTO dto = new com.greatmap.agy.api.dto.SysUserUpdateDTO();
        dto.setId(1L);
        dto.setStatus(0); // 尝试停用

        assertThrows(BusinessException.class, () -> userService.updateUser(dto),
                "停用超级管理员账号必须触发 BusinessException 拒绝");

        SecurityContextHolder.clearContext();
    }
}
