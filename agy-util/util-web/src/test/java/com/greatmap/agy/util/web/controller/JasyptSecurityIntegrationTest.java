package com.greatmap.agy.util.web.controller;

import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.security.PermissionService;
import com.greatmap.agy.common.security.TokenAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        JasyptController.class,
        PermissionService.class,
        JasyptSecurityIntegrationTest.TestConfig.class
})
@DisplayName("JasyptController Spring Security 鉴权与访问控制集成测试")
class JasyptSecurityIntegrationTest {

    @Configuration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestConfig {
    }

    @Autowired
    private JasyptController jasyptController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TokenAuthFilter tokenAuthFilter = new TokenAuthFilter(null);
        mockMvc = MockMvcBuilders.standaloneSetup(jasyptController)
                .addFilters(tokenAuthFilter)
                .build();
    }

    @Test
    @DisplayName("断言 1: HTTP 过滤器层未携带 Token 请求受限接口 -> 精准拦截并返回标准 401 JSON 错误体")
    void testFilterBlocksUnauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(post("/jasypt/encode")
                        .param("text", "MyPassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("尚未登录，请先登录系统"));
    }

    @Test
    @DisplayName("断言 2: 匿名无凭证调用接口 -> 方法级 AOP 触发 AuthorizationDeniedException 拒绝执行")
    void testAnonymousAccessDeniedByMethodSecurity() {
        assertThrows(AuthorizationDeniedException.class, () -> {
            jasyptController.encode("hello", "TEST_SALT");
        });
    }

    @Test
    @WithMockUser(username = "normal_user", roles = {"USER"})
    @DisplayName("断言 3: 普通已登录用户访问管理员接口 -> @ss.hasRole('admin') 判定不满足，拒绝访问抛出 AccessDeniedException")
    void testNormalUserAccessForbidden() {
        assertThrows(AccessDeniedException.class, () -> {
            jasyptController.encode("hello", "TEST_SALT");
        });
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"admin"})
    @DisplayName("断言 4: 管理员用户访问接口 -> @ss.hasRole('admin') 校验通过，正常执行 Jasypt 加密并返回 200")
    void testAdminUserAccessSuccess() {
        ReflectionTestUtils.setField(jasyptController, "defaultPassword", "TEST_SALT");
        Result<String> res = jasyptController.encode("hello", "TEST_SALT");
        assertNotNull(res);
        assertEquals(200, res.getCode());
        assertNotNull(res.getData());
    }
}
