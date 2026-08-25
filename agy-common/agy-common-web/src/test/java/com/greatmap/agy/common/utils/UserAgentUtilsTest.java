package com.greatmap.agy.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserAgentUtils 浏览器与操作系统解析工具类单元测试")
class UserAgentUtilsTest {

    @Test
    @DisplayName("测试空请求与空 UA 兜底返回未知")
    void testNullAndEmptyRequest() {
        assertEquals("未知浏览器", UserAgentUtils.getBrowser(null));
        assertEquals("未知系统", UserAgentUtils.getOs(null));

        MockHttpServletRequest request = new MockHttpServletRequest();
        assertEquals("未知浏览器", UserAgentUtils.getBrowser(request));
        assertEquals("未知系统", UserAgentUtils.getOs(request));
    }

    @Test
    @DisplayName("测试 macOS 上的 Chrome 浏览器解析与 OSX 规范化")
    void testChromeOnMacOs() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        String browser = UserAgentUtils.getBrowser(request);
        String os = UserAgentUtils.getOs(request);

        assertTrue(browser.contains("Chrome"));
        assertEquals("macOS", os);
    }

    @Test
    @DisplayName("测试 Windows 11 / 10 操作系统与 Edge 浏览器解析")
    void testEdgeOnWindows() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0");

        String browser = UserAgentUtils.getBrowser(request);
        String os = UserAgentUtils.getOs(request);

        assertNotNull(browser);
        assertNotNull(os);
        assertTrue(os.contains("Windows"));
    }

    @Test
    @DisplayName("测试 Chromium 150.0.0.0 冻结假版本号过滤，输出干净的浏览器品牌名")
    void testChromiumVersion150Filter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36");

        String browser = UserAgentUtils.getBrowser(request);
        assertEquals("Chrome", browser);
    }

    @Test
    @DisplayName("测试 iPhone 移动端 Safari 浏览器与 iOS 操作系统解析")
    void testIphoneSafari() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1");

        String browser = UserAgentUtils.getBrowser(request);
        String os = UserAgentUtils.getOs(request);

        assertNotNull(browser);
        assertNotNull(os);
        assertTrue(os.contains("iOS") || os.contains("iPhone"));
    }
}
