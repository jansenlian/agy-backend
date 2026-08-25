package com.greatmap.agy.common.utils;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * IP 及地理位置获取工具类 (支持可信反向代理边界校验防 IP 伪造)
 */
@Slf4j
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实 IP 地址 (具备零信任可信代理边界校验)
     * <p>
     * 1. 若直连物理 IP (getRemoteAddr) 来自可信内网/网关 (127.0.0.1 / 10.x / 172.16-31.x / 192.168.x)，信任 X-Forwarded-For / X-Real-IP Header；
     * 2. 若直连物理 IP 为外网公网 IP，说明绕过了 Nginx 直连后端，出于安全防线拒绝信任任何客户端伪造的 Header，直接使用物理 IP。
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String remoteAddr = request.getRemoteAddr();
        if (LOCALHOST_IPV6.equals(remoteAddr)) {
            remoteAddr = LOCALHOST_IPV4;
        }

        // 判断直连发起方是否为可信的内网代理/网关
        boolean isFromTrustedProxy = StrUtil.isBlank(remoteAddr)
                || LOCALHOST_IPV4.equals(remoteAddr)
                || NetUtil.isInnerIP(remoteAddr);

        // 如果直连 IP 不是可信代理 (即直连公网)，强制禁用 Header 读取，防止伪造绕过限流
        if (!isFromTrustedProxy) {
            return remoteAddr;
        }

        // 来自可信代理，按优先级提取 HTTP 传输头中的真实客户端 IP
        String ip = request.getHeader("x-forwarded-for");
        if (StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = remoteAddr;
        }

        // 多级反向代理时，取第一个非 unknown 的有效 IP
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            String[] ips = ip.split(",");
            for (String subIp : ips) {
                if (StrUtil.isNotBlank(subIp) && !UNKNOWN.equalsIgnoreCase(subIp.trim())) {
                    ip = subIp.trim();
                    break;
                }
            }
        }

        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        return ip;
    }

    /**
     * 根据 IP 地址获取地理位置 (内网识别 + 外网 API 解析)
     */
    public static String getLocation(String ip) {
        if (StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            return "未知位置";
        }

        // 局域网/内网/本机 IP 处理
        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip) || NetUtil.isInnerIP(ip)) {
            return "本地局域网";
        }

        try {
            // 使用太平洋网络免费 IP 接口查询 (带 2000ms 超时)
            String url = "http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true";
            String rspStr = HttpUtil.get(url, 2000);
            if (StrUtil.isNotBlank(rspStr)) {
                JSONObject obj = JSONUtil.parseObj(rspStr);
                String pro = obj.getStr("pro");
                String city = obj.getStr("city");
                if (StrUtil.isNotBlank(pro)) {
                    return StrUtil.isBlank(city) || pro.equals(city) ? pro : pro + " " + city;
                }
                String addr = obj.getStr("addr");
                if (StrUtil.isNotBlank(addr)) {
                    return addr.trim();
                }
            }
        } catch (Exception e) {
            log.warn("获取 IP [{}] 地理位置异常: {}", ip, e.getMessage());
        }

        return "未知位置";
    }
}
