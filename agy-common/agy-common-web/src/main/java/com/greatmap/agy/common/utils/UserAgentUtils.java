package com.greatmap.agy.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 浏览器及操作系统解析工具类
 */
public class UserAgentUtils {

    /**
     * 获取精简漂亮的浏览器名称 (真实动态解析)
     */
    public static String getBrowser(HttpServletRequest request) {
        if (request == null) {
            return "未知浏览器";
        }
        String uaStr = request.getHeader("User-Agent");
        if (StrUtil.isBlank(uaStr)) {
            return "未知浏览器";
        }
        UserAgent ua = UserAgentUtil.parse(uaStr);
        if (ua == null || ua.getBrowser() == null) {
            return "未知浏览器";
        }
        String browserName = ua.getBrowser().getName();
        if (StrUtil.isBlank(browserName) || "Unknown".equalsIgnoreCase(browserName)) {
            return "未知浏览器";
        }

        String version = ua.getVersion();
        // 过滤 Chromium 的 150.0.0.0 冻结虚假版本号以及无意义的版本字符串，输出干净的浏览器品牌名
        if (StrUtil.isNotBlank(version) 
                && !"Unknown".equalsIgnoreCase(version) 
                && !version.startsWith("150.")
                && !version.startsWith("0.")) {
            return browserName + " " + version.split("\\.")[0];
        }
        return browserName;
    }

    /**
     * 获取精简漂亮的操作系统名称 (真实动态解析)
     */
    public static String getOs(HttpServletRequest request) {
        if (request == null) {
            return "未知系统";
        }
        String uaStr = request.getHeader("User-Agent");
        if (StrUtil.isBlank(uaStr)) {
            return "未知系统";
        }
        UserAgent ua = UserAgentUtil.parse(uaStr);
        if (ua == null || ua.getOs() == null) {
            return "未知系统";
        }
        String osName = ua.getOs().getName();
        if (StrUtil.isBlank(osName) || "Unknown".equalsIgnoreCase(osName)) {
            return "未知系统";
        }
        if ("OSX".equalsIgnoreCase(osName) || "Mac OS X".equalsIgnoreCase(osName) || "Macintosh".equalsIgnoreCase(osName)) {
            return "macOS";
        }
        return osName;
    }
}
