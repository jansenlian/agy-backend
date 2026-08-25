package com.greatmap.agy.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatmap.agy.api.dto.LoginRequestDTO;
import com.greatmap.agy.api.vo.LoginResponseVO;
import com.greatmap.agy.api.vo.SysUserVO;
import com.greatmap.agy.service.service.SysLoginLogService;
import com.greatmap.agy.service.service.SysMenuService;
import com.greatmap.agy.service.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("SysAuthController 认证控制器 MockMvc 接口集成测试")
class SysAuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private SysUserService sysUserService;

    @Mock
    private SysMenuService sysMenuService;

    @Mock
    private SysLoginLogService sysLoginLogService;

    @InjectMocks
    private SysAuthController sysAuthController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysAuthController).build();
    }

    @Test
    @DisplayName("测试 /api/auth/login 登录接口成功并返回标准 Token 响应体")
    void testLoginApiSuccess() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword(com.greatmap.agy.common.utils.RsaUtils.encrypt("123456"));

        LoginResponseVO vo = new LoginResponseVO();
        vo.setToken("abcdef1234567890abcdef1234567890");
        SysUserVO userVo = new SysUserVO();
        userVo.setId(1L);
        userVo.setUsername("admin");
        userVo.setRealName("超级管理员");
        vo.setUser(userVo);

        when(sysUserService.login(any(LoginRequestDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", "116.228.88.10")
                        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("abcdef1234567890abcdef1234567890"))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.realName").value("超级管理员"));
    }

    @Test
    @DisplayName("测试 /api/auth/permissions 获取用户按钮操作权限列表接口")
    void testGetUserPermissionsApi() throws Exception {
        when(sysMenuService.getUserPermissions(any())).thenReturn(List.of("sys:user:list", "sys:user:add", "sys:user:edit"));

        mockMvc.perform(get("/api/auth/permissions")
                        .header("X-User-Id", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("sys:user:list"))
                .andExpect(jsonPath("$.data[1]").value("sys:user:add"));
    }
}
