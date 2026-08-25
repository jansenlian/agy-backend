package com.greatmap.agy.web.controller;

import cn.hutool.core.bean.BeanUtil;
import com.greatmap.agy.api.dto.LoginRequestDTO;
import com.greatmap.agy.api.vo.LoginResponseVO;
import com.greatmap.agy.api.vo.SysMenuVO;
import com.greatmap.agy.api.vo.SysUserVO;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.utils.IpUtils;
import com.greatmap.agy.common.utils.SecurityUtils;
import com.greatmap.agy.common.utils.UserAgentUtils;
import com.greatmap.agy.service.entity.SysUser;
import com.greatmap.agy.service.service.SysLoginLogService;
import com.greatmap.agy.service.service.SysMenuService;
import com.greatmap.agy.service.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "01. 认证鉴权中心")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SysAuthController {

    private final SysUserService sysUserService;
    private final SysMenuService sysMenuService;
    private final SysLoginLogService sysLoginLogService;
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @com.greatmap.agy.common.annotation.Anonymous
    @Operation(summary = "账号密码登录 (返回 32位 UUID Token)")
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequestDTO dto, HttpServletRequest request) {
        String ip = IpUtils.getClientIp(request);
        String browser = UserAgentUtils.getBrowser(request);
        String os = UserAgentUtils.getOs(request);

        // 1. IP 维度防刷限流检测 (每分钟最多允许 30 次登录尝试)
        checkIpRateLimit(ip);

        try {
            LoginResponseVO vo = sysUserService.login(dto);
            // 2. 异步记录登录成功日志 (IP 地理位置异步解析，零阻塞主请求线程)
            sysLoginLogService.recordLoginLog(dto.getUsername(), ip, browser, os, 1, "登录成功");
            return Result.success(vo);
        } catch (com.greatmap.agy.common.exception.BusinessException be) {
            log.warn("用户 [{}] 登录未通过: {}", dto.getUsername(), be.getMessage());
            // 3. 异步记录登录失败日志
            sysLoginLogService.recordLoginLog(dto.getUsername(), ip, browser, os, 0, be.getMessage());
            throw be;
        } catch (Exception e) {
            log.error("用户 [{}] 登录过程发生系统未知异常: ", dto.getUsername(), e);
            // 3. 异步记录登录失败日志
            sysLoginLogService.recordLoginLog(dto.getUsername(), ip, browser, os, 0, e.getMessage());
            throw e;
        }
    }

    private void checkIpRateLimit(String ip) {
        if (stringRedisTemplate == null || cn.hutool.core.util.StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            return;
        }
        try {
            String rateLimitKey = com.greatmap.agy.common.constant.AuthConstants.LOGIN_LIMIT_IP_PREFIX + ip;
            Long count = stringRedisTemplate.opsForValue().increment(rateLimitKey);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(rateLimitKey, java.time.Duration.ofMinutes(1));
            }
            if (count != null && count > com.greatmap.agy.common.constant.AuthConstants.MAX_IP_LOGIN_PER_MINUTE) {
                throw new com.greatmap.agy.common.exception.BusinessException("当前网络登录请求过于频繁，请稍后再试");
            }
        } catch (com.greatmap.agy.common.exception.BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.warn("IP 登录限流检测异常: {}", e.getMessage());
        }
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<SysUserVO> getLoginUserInfo() {
        Long userId = SecurityUtils.getLoginUserId();
        SysUser user = sysUserService.getById(userId);
        return Result.success(BeanUtil.copyProperties(user, SysUserVO.class));
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前登录用户树形菜单与权限标识")
    @GetMapping("/menu-tree")
    public Result<List<SysMenuVO>> getMenuTree() {
        Long userId = SecurityUtils.getLoginUserId();
        List<SysMenuVO> menuTree = sysMenuService.getUserMenuTree(userId);
        return Result.success(menuTree);
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前登录用户的按钮与操作权限标识列表")
    @GetMapping("/permissions")
    public Result<List<String>> getUserPermissions() {
        Long userId = SecurityUtils.getLoginUserId();
        List<String> permissions = sysMenuService.getUserPermissions(userId);
        return Result.ok(permissions);
    }

    @com.greatmap.agy.common.annotation.Anonymous
    @Operation(summary = "获取登录 RSA 加密公钥")
    @GetMapping("/public-key")
    public Result<String> getPublicKey() {
        return Result.ok(sysUserService.getPublicKey());
    }

    @com.greatmap.agy.common.annotation.Anonymous
    @Operation(summary = "7 天 Refresh Token 无感刷新 Access Token")
    @PostMapping("/refresh")
    public Result<LoginResponseVO> refreshToken(@RequestParam(required = false) String refreshToken,
                                               @RequestBody(required = false) java.util.Map<String, String> body) {
        String token = refreshToken;
        if (cn.hutool.core.util.StrUtil.isBlank(token) && body != null) {
            token = body.get("refreshToken");
        }
        return Result.ok(sysUserService.refreshToken(token));
    }
}
