package com.greatmap.agy.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.agy.api.dto.LoginRequestDTO;
import com.greatmap.agy.api.dto.PageQueryDTO;
import com.greatmap.agy.api.dto.SysUserCreateDTO;
import com.greatmap.agy.api.vo.LoginResponseVO;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysUserVO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.service.entity.SysRole;
import com.greatmap.agy.service.entity.SysUser;
import com.greatmap.agy.service.mapper.SysRoleMapper;
import com.greatmap.agy.service.mapper.SysUserMapper;
import com.greatmap.agy.service.mapper.SysUserRoleMapper;
import com.greatmap.agy.service.service.SysMenuService;
import com.greatmap.agy.service.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuService sysMenuService;
    private final com.greatmap.agy.common.config.SecurityProperties securityProperties;

    private long getAccessTokenTtl() {
        return (securityProperties != null && securityProperties.getToken() != null)
                ? securityProperties.getToken().getAccessTokenTtl()
                : com.greatmap.agy.common.constant.AuthConstants.TOKEN_TTL_SECONDS;
    }

    private long getRefreshTokenTtl() {
        return (securityProperties != null && securityProperties.getToken() != null)
                ? securityProperties.getToken().getRefreshTokenTtl()
                : com.greatmap.agy.common.constant.AuthConstants.REFRESH_TOKEN_TTL_SECONDS;
    }

    @Override
    public LoginResponseVO login(LoginRequestDTO dto) {
        String username = dto.getUsername();

        // 1. 检查账号是否处于锁定状态 (密码连续输错锁定 15 分钟)
        String lockKey = com.greatmap.agy.common.constant.AuthConstants.LOGIN_LOCK_PREFIX + username;
        String failKey = com.greatmap.agy.common.constant.AuthConstants.LOGIN_FAIL_PREFIX + username;

        if (stringRedisTemplate != null) {
            try {
                Boolean isLocked = stringRedisTemplate.hasKey(lockKey);
                if (Boolean.TRUE.equals(isLocked)) {
                    Long expireMinutes = stringRedisTemplate.getExpire(lockKey, java.util.concurrent.TimeUnit.MINUTES);
                    long remaining = (expireMinutes != null && expireMinutes > 0) ? expireMinutes : 1L;
                    throw new BusinessException("密码连续输入错误次数过多，账号已临时锁定，请 " + remaining + " 分钟后再试");
                }
            } catch (BusinessException be) {
                throw be;
            } catch (Exception e) {
                log.warn("检查 Redis 登录锁定状态异常: {}", e.getMessage());
            }
        }

        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            recordLoginFail(username, failKey, lockKey);
            throw new BusinessException("账号或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被冻结停用，请联系管理员");
        }

        // 2. 密码非对称解密与比对 (兼容已加密密文与测试用例未加密明文)
        String rawPassword = com.greatmap.agy.common.utils.RsaUtils.decrypt(dto.getPassword());
        if (!BCrypt.checkpw(rawPassword, user.getPassword())) {
            recordLoginFail(username, failKey, lockKey);
            throw new BusinessException("账号或密码错误");
        }

        // 3. 登录成功：清除失败计数与锁定标识
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.delete(java.util.List.of(failKey, lockKey));
            } catch (Exception ignored) {
            }
        }

        // 4. 签发双令牌：短生命周期 Access Token + 长生命周期 Refresh Token (支持 application.yml 自由配置)
        String accessToken = IdUtil.simpleUUID();
        String refreshToken = IdUtil.simpleUUID();
        String userPayload = user.getId() + ":" + user.getUsername() + ":" + System.currentTimeMillis();

        long accessTtl = getAccessTokenTtl();
        long refreshTtl = getRefreshTokenTtl();

        // 5. 提取用户实际 RBAC 权限标识集合 (用于后端 @PreAuthorize 强授权拦截)
        List<String> perms = (sysMenuService != null) ? sysMenuService.getUserPermissions(user.getId()) : List.of();

        try {
            if (stringRedisTemplate != null) {
                // 保存 Access Token
                stringRedisTemplate.opsForValue().set(
                        com.greatmap.agy.common.constant.AuthConstants.TOKEN_PREFIX + accessToken,
                        userPayload,
                        java.time.Duration.ofSeconds(accessTtl)
                );
                stringRedisTemplate.opsForValue().set(
                        com.greatmap.agy.common.constant.AuthConstants.TOKEN_PERMS_PREFIX + accessToken,
                        cn.hutool.json.JSONUtil.toJsonStr(perms),
                        java.time.Duration.ofSeconds(accessTtl)
                );
                // 保存 Refresh Token
                stringRedisTemplate.opsForValue().set(
                        com.greatmap.agy.common.constant.AuthConstants.REFRESH_TOKEN_PREFIX + refreshToken,
                        userPayload,
                        java.time.Duration.ofSeconds(refreshTtl)
                );
                // 维护用户与活跃 Token 的映射关系 (用于权限/状态/密码变更时精准清理作废 AccessToken 与 RefreshToken)
                stringRedisTemplate.opsForSet().add("user_tokens:" + user.getId(), accessToken);
                stringRedisTemplate.opsForSet().add("user_refresh_tokens:" + user.getId(), refreshToken);
                stringRedisTemplate.expire("user_tokens:" + user.getId(), java.time.Duration.ofSeconds(refreshTtl));
                stringRedisTemplate.expire("user_refresh_tokens:" + user.getId(), java.time.Duration.ofSeconds(refreshTtl));
            }
        } catch (Exception e) {
            log.error("用户 [{}] 登录令牌写入 Redis 异常 (Fail-Fast 快速失败熔断): ", dto.getUsername(), e);
            throw new BusinessException("认证服务异常，令牌写入失败，请检查缓存服务状态");
        }

        return LoginResponseVO.builder()
                .token(accessToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTtl)
                .tokenType("Bearer")
                .user(BeanUtil.copyProperties(user, SysUserVO.class))
                .build();
    }

    @Override
    public LoginResponseVO refreshToken(String refreshToken) {
        if (cn.hutool.core.util.StrUtil.isBlank(refreshToken)) {
            throw new BusinessException("Refresh Token 不能为空");
        }

        String refreshKey = com.greatmap.agy.common.constant.AuthConstants.REFRESH_TOKEN_PREFIX + refreshToken;
        String userPayload = null;

        try {
            if (stringRedisTemplate != null) {
                // 使用 Redis Lua 脚本完成 GETDEL 原子操作（读取并原子删除，彻底打消并发重放与多 Token 签发）
                String luaScript = "local val = redis.call('GET', KEYS[1]); if val then redis.call('DEL', KEYS[1]); end; return val;";
                userPayload = stringRedisTemplate.execute(
                        new org.springframework.data.redis.core.script.DefaultRedisScript<>(luaScript, String.class),
                        java.util.List.of(refreshKey)
                );
            }
        } catch (Exception e) {
            log.error("原子读取并销毁 Redis Refresh Token 异常: ", e);
            throw new BusinessException("认证服务异常，刷新令牌校验失败");
        }

        if (cn.hutool.core.util.StrUtil.isBlank(userPayload)) {
            throw new BusinessException("Refresh Token 已过期或已被使用，请重新登录");
        }

        // 解析 userId 与 原始首次登录时间戳 (继承物理起算点，彻底打消刷新 Token 绕过 24h 绝对会话上限漏洞)
        Long userId = null;
        String originalLoginTimestamp = null;
        String[] parts = userPayload.split(":");
        if (parts.length >= 1) {
            userId = Long.parseLong(parts[0]);
        }
        if (parts.length >= 3) {
            originalLoginTimestamp = parts[2];
        } else {
            originalLoginTimestamp = String.valueOf(System.currentTimeMillis());
        }

        SysUser user = this.getById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BusinessException("用户不存在或账号已被停用");
        }

        // 重新签发新的 Access Token 和 全新的 Refresh Token (RefreshToken 轮换机制 - RTR，继承原始 loginTimestamp)
        String newAccessToken = IdUtil.simpleUUID();
        String newRefreshToken = IdUtil.simpleUUID();
        long accessTtl = getAccessTokenTtl();
        long refreshTtl = getRefreshTokenTtl();
        String newUserPayload = user.getId() + ":" + user.getUsername() + ":" + originalLoginTimestamp;
        List<String> perms = (sysMenuService != null) ? sysMenuService.getUserPermissions(user.getId()) : List.of();
        try {
            if (stringRedisTemplate != null) {
                // 写入新的 Access Token 及对应权限集
                stringRedisTemplate.opsForValue().set(
                        com.greatmap.agy.common.constant.AuthConstants.TOKEN_PREFIX + newAccessToken,
                        newUserPayload,
                        java.time.Duration.ofSeconds(accessTtl)
                );
                stringRedisTemplate.opsForValue().set(
                        com.greatmap.agy.common.constant.AuthConstants.TOKEN_PERMS_PREFIX + newAccessToken,
                        cn.hutool.json.JSONUtil.toJsonStr(perms),
                        java.time.Duration.ofSeconds(accessTtl)
                );

                // 写入全新的 Refresh Token
                stringRedisTemplate.opsForValue().set(
                        com.greatmap.agy.common.constant.AuthConstants.REFRESH_TOKEN_PREFIX + newRefreshToken,
                        newUserPayload,
                        java.time.Duration.ofSeconds(refreshTtl)
                );

                // 维护用户与活跃 Token 的映射关系 (同步维护 AccessToken 与 RefreshToken 集合)
                stringRedisTemplate.opsForSet().add("user_tokens:" + user.getId(), newAccessToken);
                stringRedisTemplate.opsForSet().add("user_refresh_tokens:" + user.getId(), newRefreshToken);
                stringRedisTemplate.expire("user_tokens:" + user.getId(), java.time.Duration.ofSeconds(refreshTtl));
                stringRedisTemplate.expire("user_refresh_tokens:" + user.getId(), java.time.Duration.ofSeconds(refreshTtl));
            }
        } catch (Exception e) {
            log.error("用户 ID [{}] 刷新令牌写入 Redis 异常 (Fail-Fast 快速失败熔断): ", userId, e);
            throw new BusinessException("令牌刷新失败，缓存写入异常，请重新登录");
        }

        return LoginResponseVO.builder()
                .token(newAccessToken)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(accessTtl)
                .tokenType("Bearer")
                .user(BeanUtil.copyProperties(user, SysUserVO.class))
                .build();
    }

    @Override
    public String getPublicKey() {
        return com.greatmap.agy.common.utils.RsaUtils.getPublicKeyBase64();
    }

    @Override
    public PageResultVO<SysUserVO> getUserPage(PageQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = this.page(page, wrapper);

        List<SysUser> users = userPage != null ? userPage.getRecords() : List.of();
        if (users == null || users.isEmpty()) {
            return new PageResultVO<>(List.of(), userPage != null ? userPage.getTotal() : 0L, 
                    userPage != null ? userPage.getSize() : queryDTO.getPageSize(), 
                    userPage != null ? userPage.getCurrent() : queryDTO.getPageNo(), 
                    userPage != null ? userPage.getPages() : 0L);
        }

        // 🚀 性能优化：1 次批量查询所有用户的角色关联，彻底消除每页 N 次循环单查的 N+1 SQL 瓶颈
        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<com.greatmap.agy.api.dto.SysUserRoleDTO> userRoles = sysRoleMapper.selectRoleNamesByUserIds(userIds);
        java.util.Map<Long, List<String>> userRolesMap = new java.util.HashMap<>();
        if (userRoles != null) {
            for (com.greatmap.agy.api.dto.SysUserRoleDTO item : userRoles) {
                if (item.getUserId() != null && item.getRoleName() != null) {
                    userRolesMap.computeIfAbsent(item.getUserId(), k -> new java.util.ArrayList<>()).add(item.getRoleName());
                }
            }
        }

        List<SysUserVO> voList = users.stream().map(user -> {
            SysUserVO vo = BeanUtil.copyProperties(user, SysUserVO.class);
            if (vo.getMobile() != null && vo.getMobile().length() == 11) {
                vo.setMobile(vo.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
            }
            vo.setRoleNames(userRolesMap.getOrDefault(user.getId(), List.of()));
            return vo;
        }).toList();

        return new PageResultVO<>(voList, userPage.getTotal(), userPage.getSize(), userPage.getCurrent(), userPage.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(SysUserCreateDTO dto) {
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        String plainPassword = com.greatmap.agy.common.utils.RsaUtils.decrypt(dto.getPassword());
        user.setPassword(BCrypt.hashpw(plainPassword));
        user.setRealName(dto.getRealName());
        user.setMobile(dto.getMobile());
        user.setStatus(1);

        this.save(user);
        return user.getId();
    }

    /**
     * 判断目标用户是否为系统内置超级管理员 (id=1 或 username=admin)
     */
    private boolean isSuperAdminUser(SysUser user) {
        return user != null && (Long.valueOf(1L).equals(user.getId()) || "admin".equalsIgnoreCase(user.getUsername()));
    }

    /**
     * 判断当前登录操作人是否为超级管理员 (id=1 或拥有 ROLE_ADMIN 角色)
     */
    private boolean isCurrentSuperAdmin() {
        Long currentUserId = com.greatmap.agy.common.utils.SecurityUtils.getLoginUserId();
        if (currentUserId == null) {
            return false;
        }
        if (currentUserId == 1L) {
            return true;
        }
        List<String> roleCodes = sysUserRoleMapper.selectRoleCodesByUserId(currentUserId);
        return roleCodes != null && roleCodes.contains("ROLE_ADMIN");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(com.greatmap.agy.api.dto.SysUserUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户 ID 不能为空");
        }
        SysUser oldUser = this.getById(dto.getId());
        if (oldUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 🛡️ 越权防线：超级管理员账号保护
        if (isSuperAdminUser(oldUser)) {
            if (!isCurrentSuperAdmin()) {
                throw new BusinessException("越权拒绝：普通管理员禁止修改内置超级管理员信息");
            }
            if (dto.getStatus() != null && dto.getStatus() == 0) {
                throw new BusinessException("操作拒绝：系统内置超级管理员账号禁止停用");
            }
        }

        if (cn.hutool.core.util.StrUtil.isNotBlank(dto.getRealName())) {
            oldUser.setRealName(dto.getRealName());
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(dto.getMobile())) {
            oldUser.setMobile(dto.getMobile());
        }
        if (dto.getStatus() != null) {
            oldUser.setStatus(dto.getStatus());
            if (dto.getStatus() == 0) {
                // 🔴 核心加固：用户被置为禁用 (status = 0) 时，必须立即在 Redis 中全量级联作废其 AccessToken 与 RefreshToken！
                evictUserTokens(dto.getId());
            }
        }
        return this.updateById(oldUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 🛡️ 越权防线：超级管理员账号密码重置保护 (非超级管理员禁止重置超管密码)
        if (isSuperAdminUser(user) && !isCurrentSuperAdmin()) {
            throw new BusinessException("越权拒绝：只有超级管理员才可以重置超级管理员账号密码");
        }

        if (StringUtils.isBlank(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        String plainPassword;
        try {
            plainPassword = com.greatmap.agy.common.utils.RsaUtils.decrypt(newPassword);
        } catch (Exception e) {
            throw new BusinessException("安全拒绝：重置密码必须经前端 RSA 公钥加密传输");
        }
        if (StringUtils.isBlank(plainPassword) || plainPassword.length() < 6) {
            throw new BusinessException("新密码解密失败或密码长度少于 6 位");
        }
        user.setPassword(BCrypt.hashpw(plainPassword));
        boolean success = this.updateById(user);
        if (success) {
            evictUserTokens(userId);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new BusinessException("请先登录账号");
        }
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("当前账号不存在");
        }
        String plainOld = com.greatmap.agy.common.utils.RsaUtils.decrypt(oldPassword);
        String plainNew = com.greatmap.agy.common.utils.RsaUtils.decrypt(newPassword);
        if (!BCrypt.checkpw(plainOld, user.getPassword())) {
            throw new BusinessException("原密码不正确，请重新输入");
        }
        if (StringUtils.isBlank(plainNew) || plainNew.length() < 6) {
            throw new BusinessException("新密码长度不能少于 6 位");
        }
        user.setPassword(BCrypt.hashpw(plainNew));
        boolean success = this.updateById(user);
        if (success) {
            // 🔴 核心加固：个人修改密码成功后，立即全量作废既有 AccessToken 与 RefreshToken，强制重新登录！
            evictUserTokens(userId);
        }
        return success;
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return sysUserRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            throw new BusinessException("用户 ID 不能为空");
        }

        // 越权提权防线 (Privilege Escalation Protection)
        Long currentUserId = com.greatmap.agy.common.utils.SecurityUtils.getLoginUserId();
        if (currentUserId != null && !isCurrentSuperAdmin()) {
            // 1. 企图给任何用户赋予 ROLE_ADMIN 角色 -> 拒绝
            if (roleIds != null && !roleIds.isEmpty()) {
                List<SysRole> targetRoles = sysRoleMapper.selectBatchIds(roleIds);
                for (SysRole r : targetRoles) {
                    if (r != null && (r.getId() == 1L || "ROLE_ADMIN".equalsIgnoreCase(r.getRoleCode()))) {
                        throw new BusinessException("越权拒绝：只有超级管理员才可以授予超级管理员角色");
                    }
                }

                // 2. 子集原则：非超级管理员只能分配自己名下已拥有的角色
                List<Long> currentUserRoleIds = sysUserRoleMapper.selectRoleIdsByUserId(currentUserId);
                java.util.Set<Long> currentUserRoleSet = new java.util.HashSet<>(currentUserRoleIds != null ? currentUserRoleIds : List.of());
                for (Long roleId : roleIds) {
                    if (!currentUserRoleSet.contains(roleId)) {
                        throw new BusinessException("越权拒绝：您无法将本人未获授权的角色分配给其他用户");
                    }
                }
            }

            // 3. 防降级：非超级管理员不能剥夺 userId = 1L 的超级管理员角色
            SysUser targetUser = this.getById(userId);
            if (isSuperAdminUser(targetUser)) {
                throw new BusinessException("越权拒绝：普通管理员禁止修改内置超级管理员账号的角色");
            }
        }

        sysUserRoleMapper.deleteByUserId(userId);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                sysUserRoleMapper.insertUserRole(userId, roleId);
            }
        }

        // 🔴 核心加固：用户角色一旦变更，必须立即在 Redis 中作废该用户的活跃 Token 与权限缓存 (防旧权限滞后与逃逸)
        evictUserTokens(userId);

        return true;
    }

    public void evictUserTokens(Long userId) {
        if (userId == null || stringRedisTemplate == null) {
            return;
        }
        try {
            // 1. 作废关联的 AccessToken 及其权限缓存
            String userTokensKey = "user_tokens:" + userId;
            java.util.Set<String> tokens = stringRedisTemplate.opsForSet().members(userTokensKey);
            if (tokens != null && !tokens.isEmpty()) {
                for (String token : tokens) {
                    stringRedisTemplate.delete(com.greatmap.agy.common.constant.AuthConstants.TOKEN_PREFIX + token);
                    stringRedisTemplate.delete(com.greatmap.agy.common.constant.AuthConstants.TOKEN_PERMS_PREFIX + token);
                }
            }
            stringRedisTemplate.delete(userTokensKey);

            // 2. 级联作废关联的 RefreshToken (彻底切断以旧换新二次提权漏洞)
            String userRefreshTokensKey = "user_refresh_tokens:" + userId;
            java.util.Set<String> refreshTokens = stringRedisTemplate.opsForSet().members(userRefreshTokensKey);
            if (refreshTokens != null && !refreshTokens.isEmpty()) {
                for (String rToken : refreshTokens) {
                    stringRedisTemplate.delete(com.greatmap.agy.common.constant.AuthConstants.REFRESH_TOKEN_PREFIX + rToken);
                }
            }
            stringRedisTemplate.delete(userRefreshTokensKey);
        } catch (Exception e) {
            log.warn("作废用户 [{}] Redis 全量会话 Token 缓存异常: {}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户 ID 不能为空");
        }
        if (userId == 1L) {
            throw new BusinessException("系统超级管理员内置账号禁止删除");
        }
        SysUser user = this.getById(userId);
        if (isSuperAdminUser(user)) {
            throw new BusinessException("系统超级管理员内置账号禁止删除");
        }
        // 级联清理用户角色中间表并作废 Redis Token 缓存
        sysUserRoleMapper.deleteByUserId(userId);
        evictUserTokens(userId);
        return this.removeById(userId);
    }

    private void recordLoginFail(String username, String failKey, String lockKey) {
        if (stringRedisTemplate == null || stringRedisTemplate.opsForValue() == null) {
            return;
        }
        try {
            Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
            if (failCount != null && failCount == 1L) {
                stringRedisTemplate.expire(failKey, java.time.Duration.ofSeconds(com.greatmap.agy.common.constant.AuthConstants.LOGIN_LOCK_TTL_SECONDS));
            }
            if (failCount != null && failCount >= com.greatmap.agy.common.constant.AuthConstants.MAX_LOGIN_FAIL_COUNT) {
                stringRedisTemplate.opsForValue().set(
                        lockKey,
                        "locked",
                        java.time.Duration.ofSeconds(com.greatmap.agy.common.constant.AuthConstants.LOGIN_LOCK_TTL_SECONDS)
                );
                stringRedisTemplate.delete(failKey);
                throw new BusinessException("密码连续错误已达 " + com.greatmap.agy.common.constant.AuthConstants.MAX_LOGIN_FAIL_COUNT + " 次，账号已被临时锁定 15 分钟");
            }
            long remaining = com.greatmap.agy.common.constant.AuthConstants.MAX_LOGIN_FAIL_COUNT - (failCount != null ? failCount : 0L);
            if (remaining > 0) {
                throw new BusinessException("账号或密码错误，还可尝试 " + remaining + " 次");
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.warn("记录登录失败次数异常: {}", e.getMessage());
        }
    }
}
