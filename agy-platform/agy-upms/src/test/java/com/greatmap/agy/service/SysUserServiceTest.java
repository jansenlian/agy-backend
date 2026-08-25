package com.greatmap.agy.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.greatmap.agy.api.dto.LoginRequestDTO;
import com.greatmap.agy.api.vo.LoginResponseVO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.service.entity.SysUser;
import com.greatmap.agy.service.mapper.SysRoleMapper;
import com.greatmap.agy.service.mapper.SysUserMapper;
import com.greatmap.agy.service.mapper.SysUserRoleMapper;
import com.greatmap.agy.service.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SysUserService 用户核心认证业务逻辑真实单元测试")
class SysUserServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private org.springframework.data.redis.core.SetOperations<String, String> setOperations;
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private com.greatmap.agy.service.service.SysMenuService sysMenuService;

    @Spy
    @InjectMocks
    private SysUserServiceImpl sysUserService;

    @Test
    @DisplayName("测试用户登录成功：验证 RSA 解密与 BCrypt 密码匹配并签发 32位 UUID Token")
    void testLoginSuccess() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword(com.greatmap.agy.common.utils.RsaUtils.encrypt("123456"));

        SysUser mockUser = new SysUser();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setRealName("超级管理员");
        mockUser.setPassword(BCrypt.hashpw("123456"));
        mockUser.setStatus(1);

        doReturn(mockUser).when(sysUserService).getOne(any(LambdaQueryWrapper.class));
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);

        LoginResponseVO vo = sysUserService.login(dto);

        assertNotNull(vo);
        assertNotNull(vo.getToken());
        assertEquals(32, vo.getToken().length(), "Token 必须为 32 位 UUID");
        assertEquals("admin", vo.getUser().getUsername());
        assertEquals("超级管理员", vo.getUser().getRealName());
    }

    @Test
    @DisplayName("测试传入未加密非法明文密码时严格 Fail-Closed 阻断登录")
    void testLoginWithPlainPasswordFailsClosed() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword("123456"); // 未加密明文

        SysUser mockUser = new SysUser();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setPassword(BCrypt.hashpw("123456"));
        mockUser.setStatus(1);

        doReturn(mockUser).when(sysUserService).getOne(any(LambdaQueryWrapper.class));

        BusinessException ex = assertThrows(BusinessException.class, () -> sysUserService.login(dto));
        assertTrue(ex.getMessage().contains("密码传输凭证无效或解密失败"));
    }

    @Test
    @DisplayName("测试账号不存在时阻断登录并抛出业务异常")
    void testLoginUserNotFound() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("non_existent_user");
        dto.setPassword(com.greatmap.agy.common.utils.RsaUtils.encrypt("123456"));

        doReturn(null).when(sysUserService).getOne(any(LambdaQueryWrapper.class));

        BusinessException ex = assertThrows(BusinessException.class, () -> sysUserService.login(dto));
        assertEquals("账号或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("测试密码错误时阻断登录并抛出业务异常")
    void testLoginWrongPassword() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword(com.greatmap.agy.common.utils.RsaUtils.encrypt("wrong_password_999"));

        SysUser mockUser = new SysUser();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setPassword(BCrypt.hashpw("correct_password"));
        mockUser.setStatus(1);

        doReturn(mockUser).when(sysUserService).getOne(any(LambdaQueryWrapper.class));

        BusinessException ex = assertThrows(BusinessException.class, () -> sysUserService.login(dto));
        assertEquals("账号或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("测试账号被冻结禁用 (status = 0) 时阻断登录")
    void testLoginFrozenUser() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("frozen_user");
        dto.setPassword(com.greatmap.agy.common.utils.RsaUtils.encrypt("123456"));

        SysUser mockUser = new SysUser();
        mockUser.setId(2L);
        mockUser.setUsername("frozen_user");
        mockUser.setPassword(BCrypt.hashpw("123456"));
        mockUser.setStatus(0); // 冻结

        doReturn(mockUser).when(sysUserService).getOne(any(LambdaQueryWrapper.class));

        BusinessException ex = assertThrows(BusinessException.class, () -> sysUserService.login(dto));
        assertTrue(ex.getMessage().contains("禁用") || ex.getMessage().contains("停用"));
    }

    @Test
    @DisplayName("测试修改密码时原密码校验失败")
    void testUpdatePasswordWithWrongOldPassword() {
        Long userId = 1L;
        SysUser mockUser = new SysUser();
        mockUser.setId(userId);
        mockUser.setPassword(BCrypt.hashpw("real_old_pw"));

        doReturn(mockUser).when(sysUserService).getById(userId);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                sysUserService.updatePassword(
                        userId,
                        com.greatmap.agy.common.utils.RsaUtils.encrypt("wrong_old_pw"),
                        com.greatmap.agy.common.utils.RsaUtils.encrypt("new_password_123")
                )
        );
        assertEquals("原密码不正确，请重新输入", ex.getMessage());
    }

    @Test
    @DisplayName("测试账号因连续输错密码被临时锁定阻断登录")
    void testLoginAccountLockedDueToFailures() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword(com.greatmap.agy.common.utils.RsaUtils.encrypt("123456"));

        when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
        when(stringRedisTemplate.getExpire(anyString(), any(java.util.concurrent.TimeUnit.class))).thenReturn(15L);

        BusinessException ex = assertThrows(BusinessException.class, () -> sysUserService.login(dto));
        assertTrue(ex.getMessage().contains("临时锁定"));
    }

    @Test
    @DisplayName("测试输错密码时提示剩余重试次数")
    void testLoginFailureDecrementsRemainingAttempts() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword(com.greatmap.agy.common.utils.RsaUtils.encrypt("wrong_pw"));

        SysUser mockUser = new SysUser();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setPassword(BCrypt.hashpw("correct_pw"));
        mockUser.setStatus(1);

        doReturn(mockUser).when(sysUserService).getOne(any(LambdaQueryWrapper.class));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(2L);

        BusinessException ex = assertThrows(BusinessException.class, () -> sysUserService.login(dto));
        assertTrue(ex.getMessage().contains("还可尝试 3 次"));
    }

    @Test
    @DisplayName("测试用户分页查询：验证批量关联角色并正确映射到 SysUserVO 的 roleNames")
    void testGetUserPageWithRoles() {
        com.greatmap.agy.api.dto.PageQueryDTO queryDTO = new com.greatmap.agy.api.dto.PageQueryDTO();
        queryDTO.setPageNo(1);
        queryDTO.setPageSize(10);

        SysUser user1 = new SysUser();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setRealName("超级管理员");
        user1.setMobile("13800008000");
        user1.setStatus(1);

        SysUser user2 = new SysUser();
        user2.setId(2L);
        user2.setUsername("wangjian");
        user2.setRealName("王健");
        user2.setMobile("13200005511");
        user2.setStatus(1);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser> mockPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        mockPage.setRecords(java.util.List.of(user1, user2));
        mockPage.setTotal(2);

        doReturn(mockPage).when(sysUserService).page(any(), any());

        com.greatmap.agy.api.dto.SysUserRoleDTO ur1 = new com.greatmap.agy.api.dto.SysUserRoleDTO();
        ur1.setUserId(1L);
        ur1.setRoleName("超级管理员");

        com.greatmap.agy.api.dto.SysUserRoleDTO ur2 = new com.greatmap.agy.api.dto.SysUserRoleDTO();
        ur2.setUserId(2L);
        ur2.setRoleName("普通管理员");

        com.greatmap.agy.api.dto.SysUserRoleDTO ur3 = new com.greatmap.agy.api.dto.SysUserRoleDTO();
        ur3.setUserId(2L);
        ur3.setRoleName("业务人员");

        when(sysRoleMapper.selectRoleNamesByUserIds(java.util.List.of(1L, 2L)))
                .thenReturn(java.util.List.of(ur1, ur2, ur3));

        com.greatmap.agy.api.vo.PageResultVO<com.greatmap.agy.api.vo.SysUserVO> result = 
                sysUserService.getUserPage(queryDTO);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals(java.util.List.of("超级管理员"), result.getRecords().get(0).getRoleNames());
        assertEquals(java.util.List.of("普通管理员", "业务人员"), result.getRecords().get(1).getRoleNames());
    }
}

