package com.greatmap.agy.common.utils;

import com.greatmap.agy.common.constant.AuthConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityUtils 安全上下文与用户信息提取工具类单元测试")
class SecurityUtilsTest {

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("测试在没有上下文时安全返回 null（杜绝默认管理员越权漏洞）")
    void testNoContext() {
        RequestContextHolder.resetRequestAttributes();
        assertNull(SecurityUtils.getLoginUserId());
        assertNull(SecurityUtils.getLoginUserName());
    }

    @Test
    @DisplayName("测试未登录/未传递合法验签属性时返回 null")
    void testGetLoginUserNameFallback() {
        assertNull(SecurityUtils.getLoginUserId());
        assertNull(SecurityUtils.getLoginUserName());
    }

    @Test
    @DisplayName("测试直接伪造 X-User-Id 请求头被彻底忽略并返回 null（杜绝 Header 伪造越权）")
    void testForgeHeaderIgnored() {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "admin");

        assertNull(SecurityUtils.getLoginUserId(), "严禁直接信任未验签的客户端 X-User-Id 请求头");
        assertNull(SecurityUtils.getLoginUserName(), "严禁直接信任未验签的客户端 X-User-Name 请求头");
    }

    @Test
    @DisplayName("测试成功提取经过 Filter 验签注入的雪花算法长整型用户 ID")
    void testGetLoginUserIdSuccess() {
        request.setAttribute(AuthConstants.ATTR_LOGIN_USER_ID, 2086631853247135746L);
        Long userId = SecurityUtils.getLoginUserId();
        assertNotNull(userId);
        assertEquals(2086631853247135746L, userId);
    }

    @Test
    @DisplayName("测试成功提取经过 Filter 验签注入的登录用户名")
    void testGetLoginUserNameSuccess() {
        request.setAttribute(AuthConstants.ATTR_LOGIN_USER_NAME, "审计管理员");
        String username = SecurityUtils.getLoginUserName();
        assertEquals("审计管理员", username);
    }
}
