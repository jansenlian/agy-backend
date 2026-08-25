package com.greatmap.agy.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IpUtils 客户端 IP 与地理位置工具类单元测试")
class IpUtilsTest {

    @Test
    @DisplayName("测试空请求返回 unknown")
    void testGetClientIpWithNull() {
        assertEquals("unknown", IpUtils.getClientIp(null));
    }

    @Test
    @DisplayName("测试直接通过 getRemoteAddr 获取本地回环 IP")
    void testGetClientIpDirect() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        String ip = IpUtils.getClientIp(request);
        assertEquals("127.0.0.1", ip);
    }

    @Test
    @DisplayName("测试可信代理传输 X-Forwarded-For 单 IP 提取")
    void testGetClientIpWithXForwardedForSingle() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "116.228.88.10");
        request.setRemoteAddr("10.0.0.1"); // 可信内网代理

        String ip = IpUtils.getClientIp(request);
        assertEquals("116.228.88.10", ip);
    }

    @Test
    @DisplayName("测试可信代理传输 X-Forwarded-For 多级反向代理链解析第一个真实 IP")
    void testGetClientIpWithXForwardedForMultiProxy() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "116.228.88.10, 10.0.0.2, 172.16.0.3");
        request.setRemoteAddr("10.0.0.1"); // 可信内网代理

        String ip = IpUtils.getClientIp(request);
        assertEquals("116.228.88.10", ip);
    }

    @Test
    @DisplayName("测试可信代理传输 X-Real-IP 头部提取")
    void testGetClientIpWithXRealIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "202.96.128.86");
        request.setRemoteAddr("10.0.0.1"); // 可信内网代理

        String ip = IpUtils.getClientIp(request);
        assertEquals("202.96.128.86", ip);
    }

    @Test
    @DisplayName("测试非可信公网直连时拒绝信任伪造 Header，强制锁定物理 IP")
    void testGetClientIpUntrustedDirectPublicIpIgnoresSpoofedHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "8.8.8.8");
        request.addHeader("X-Real-IP", "1.1.1.1");
        request.setRemoteAddr("203.0.113.5"); // 外网公网直连 IP (非可信代理)

        String ip = IpUtils.getClientIp(request);
        assertEquals("203.0.113.5", ip, "外网公网直连时必须拒绝信任伪造 Header 并强行返回物理连接 IP");
    }

    @Test
    @DisplayName("测试 IPv6 本地回环转换为 IPv4 127.0.0.1")
    void testGetClientIpIpv6Conversion() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("0:0:0:0:0:0:0:1");

        String ip = IpUtils.getClientIp(request);
        assertEquals("127.0.0.1", ip);
    }

    @Test
    @DisplayName("测试本地局域网 IP 地理位置识别")
    void testGetLocationLocalNetwork() {
        assertEquals("本地局域网", IpUtils.getLocation("127.0.0.1"));
        assertEquals("本地局域网", IpUtils.getLocation("192.168.1.100"));
        assertEquals("本地局域网", IpUtils.getLocation("10.10.5.20"));
        assertEquals("本地局域网", IpUtils.getLocation("172.16.0.1"));
    }

    @Test
    @DisplayName("测试未知 IP 地址返回未知位置")
    void testGetLocationUnknown() {
        assertEquals("未知位置", IpUtils.getLocation(null));
        assertEquals("未知位置", IpUtils.getLocation(""));
        assertEquals("未知位置", IpUtils.getLocation("unknown"));
    }
}
