package com.greatmap.agy.common.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.greatmap.agy.common.config.SecurityProperties;
import com.greatmap.agy.common.constant.AuthConstants;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * 全局 Token 认证与鉴权拦截过滤器
 * 支持在 application.yml 中自定义配置 Token 有效期与白名单放行
 */
@Slf4j
public class TokenAuthFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public TokenAuthFilter(StringRedisTemplate stringRedisTemplate) {
        this(stringRedisTemplate, new SecurityProperties());
    }

    public TokenAuthFilter(StringRedisTemplate stringRedisTemplate, SecurityProperties securityProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.securityProperties = securityProperties != null ? securityProperties : new SecurityProperties();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 1. 白名单直接放行 (统一引用 AuthConstants.WHITE_LIST 单一真理源)
        for (String pattern : AuthConstants.WHITE_LIST) {
            if (pathMatcher.match(pattern, uri)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 2. 提取客户端 Token (优先从 Authorization: Bearer <token>，其次从 token Header)
        String token = extractToken(request);

        if (StrUtil.isBlank(token)) {
            renderUnauthorizedResponse(response, "尚未登录，请先登录系统");
            return;
        }

        // 3. 校验 Redis 中的 Token 与权限数据 (使用 multiGet 将 2 次独立网络读取合并为 1 次 RTT)
        String redisKey = AuthConstants.TOKEN_PREFIX + token;
        String permsKey = AuthConstants.TOKEN_PERMS_PREFIX + token;
        String userPayload = null;
        String permsJson = null;

        if (stringRedisTemplate != null) {
            try {
                List<String> values = stringRedisTemplate.opsForValue().multiGet(java.util.Arrays.asList(redisKey, permsKey));
                if (values != null && !values.isEmpty()) {
                    userPayload = values.get(0);
                    if (values.size() > 1) {
                        permsJson = values.get(1);
                    }
                }
            } catch (Exception e) {
                log.warn("批量读取 Redis Token 异常，容错降级处理: {}", e.getMessage());
            }
        }

        if (StrUtil.isBlank(userPayload)) {
            renderUnauthorizedResponse(response, "登录 Token 已失效或过期，请重新登录");
            return;
        }

        // 4. 解析用户身份 (支持格式: "userId:username:loginTimestamp", "userId:username" 或纯 "userId")
        Long userId = null;
        String username = "用户";
        long loginTimestamp = System.currentTimeMillis();
        if (userPayload.contains(":")) {
            String[] parts = userPayload.split(":");
            try {
                if (parts.length >= 1) userId = Long.parseLong(parts[0]);
                if (parts.length >= 2) username = parts[1];
                if (parts.length >= 3) loginTimestamp = Long.parseLong(parts[2]);
            } catch (Exception ignored) {
            }
        } else {
            try {
                userId = Long.parseLong(userPayload);
            } catch (Exception ignored) {
            }
        }

        // 绝对最大会话存活时长上限防线 (从 SecurityProperties 中读取，默认 24 小时，彻底防线无限滑动续期)
        long maxAbsoluteSessionSeconds = (securityProperties != null && securityProperties.getToken() != null)
                ? securityProperties.getToken().getMaxSessionTtl()
                : 86400L;
        long elapsedSeconds = (System.currentTimeMillis() - loginTimestamp) / 1000L;
        if (elapsedSeconds > maxAbsoluteSessionSeconds) {
            if (stringRedisTemplate != null) {
                try {
                    stringRedisTemplate.delete(java.util.Arrays.asList(redisKey, permsKey));
                } catch (Exception ignored) {
                }
            }
            renderUnauthorizedResponse(response, "会话已达到最大存活上限(" + (maxAbsoluteSessionSeconds / 3600) + "小时)，请重新登录");
            return;
        }

        // 5. 绑定到 Request 属性中供 SecurityUtils 安全提取（彻底杜绝客户端伪造 Header 越权）
        request.setAttribute(AuthConstants.ATTR_LOGIN_USER_ID, userId);
        request.setAttribute(AuthConstants.ATTR_LOGIN_USER_NAME, username);

        // 6. Token 自动滑动续期 (使用 Pipeline 管道将双 Key 续期合并为 1 次批量写)
        if (stringRedisTemplate != null) {
            try {
                long ttl = (securityProperties != null && securityProperties.getToken() != null)
                        ? securityProperties.getToken().getAccessTokenTtl()
                        : AuthConstants.TOKEN_TTL_SECONDS;
                stringRedisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                    byte[] rawRedisKey = stringRedisTemplate.getStringSerializer().serialize(redisKey);
                    byte[] rawPermsKey = stringRedisTemplate.getStringSerializer().serialize(permsKey);
                    if (rawRedisKey != null) connection.keyCommands().expire(rawRedisKey, ttl);
                    if (rawPermsKey != null) connection.keyCommands().expire(rawPermsKey, ttl);
                    return null;
                });
            } catch (Exception ignored) {
            }
        }

        // 7. 加载用户的真实 RBAC 权限与角色集合 (解析 multiGet 预取的权限 JSON)
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_AUTHENTICATED"));

        if (StrUtil.isNotBlank(permsJson)) {
            try {
                List<String> permList = cn.hutool.json.JSONUtil.toList(permsJson, String.class);
                if (cn.hutool.core.collection.CollUtil.isNotEmpty(permList)) {
                    for (String perm : permList) {
                        if (StrUtil.isNotBlank(perm)) {
                            authorities.add(new SimpleGrantedAuthority(perm.trim()));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析 Redis 用户权限集合异常: {}", e.getMessage());
            }
        }

        // 8. 构建 Spring Security 完整鉴权上下文 (包含由 RBAC 数据库/Redis 算出的真实权限与角色集合)
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, null, authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AuthConstants.HEADER_AUTHORIZATION);
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            return authHeader.substring(AuthConstants.BEARER_PREFIX.length()).trim();
        }
        String tokenHeader = request.getHeader(AuthConstants.HEADER_TOKEN);
        if (StrUtil.isNotBlank(tokenHeader)) {
            return tokenHeader.trim();
        }
        return null;
    }

    private void renderUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> errorResult = Result.error(ErrorCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(JSONUtil.toJsonStr(errorResult));
    }
}
