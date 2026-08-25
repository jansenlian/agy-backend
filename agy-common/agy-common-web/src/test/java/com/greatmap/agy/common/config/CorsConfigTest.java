package com.greatmap.agy.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorsConfig 跨域配置与白名单安全机制单元测试")
class CorsConfigTest {

    @Test
    @DisplayName("测试允许的白名单来源能够正常放行并附带 CORS 响应头")
    void testCorsAllowedOrigin() throws Exception {
        CorsConfig corsConfig = new CorsConfig();
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        CorsFilter corsFilter = new CorsFilter(source);

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/permissions");
        request.addHeader("Origin", "http://localhost:5006");
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Access-Control-Request-Headers", "authorization,content-type");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        corsFilter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals("http://localhost:5006", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
        assertNotNull(response.getHeader("Access-Control-Allow-Methods"));
        assertNotNull(response.getHeader("Access-Control-Allow-Headers"));
    }

    @Test
    @DisplayName("测试未授权的恶意来源（非白名单）被 CORS 拦截（不返回 Allow-Origin）")
    void testCorsDisallowedOrigin() throws Exception {
        CorsConfig corsConfig = new CorsConfig();
        // 设置精准生产白名单: 仅允许 https://admin.greatmap.com
        SecurityProperties properties = new SecurityProperties();
        properties.getCors().setAllowedOriginPatterns(Collections.singletonList("https://admin.greatmap.com"));

        // 通过反射或重新调用获取 source
        org.springframework.test.util.ReflectionTestUtils.setField(corsConfig, "securityProperties", properties);
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        CorsFilter corsFilter = new CorsFilter(source);

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/permissions");
        request.addHeader("Origin", "http://evil-attacker.com");
        request.addHeader("Access-Control-Request-Method", "GET");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        corsFilter.doFilter(request, response, filterChain);

        // 非白名单来源不得返回 Access-Control-Allow-Origin
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }
}
