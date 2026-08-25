package com.greatmap.agy.common.utils;

import cn.hutool.core.util.StrUtil;
import com.greatmap.agy.common.constant.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 安全工具类 (优先从经过 TokenAuthFilter 验签的 Request 属性中提取用户身份，异步线程回退 SecurityContextHolder)
 */
public class SecurityUtils {

    /**
     * 获取当前经过安全验签的登录用户 ID (未登录返回 null)
     */
    public static Long getLoginUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object userIdObj = request.getAttribute(AuthConstants.ATTR_LOGIN_USER_ID);
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            } else if (userIdObj instanceof String && !((String) userIdObj).isBlank()) {
                try {
                    return Long.parseLong(((String) userIdObj).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 异步线程或非 Servlet 请求场景回退从 SecurityContextHolder 提取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            } else if (principal instanceof Number) {
                return ((Number) principal).longValue();
            } else if (principal instanceof String str && StrUtil.isNotBlank(str)) {
                try {
                    return Long.parseLong(str.trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return null;
    }

    /**
     * 获取当前经过安全验签的登录用户名 (未登录返回 null)
     */
    public static String getLoginUserName() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object nameObj = request.getAttribute(AuthConstants.ATTR_LOGIN_USER_NAME);
            if (nameObj != null) {
                return String.valueOf(nameObj);
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }

        return null;
    }
}
