package com.greatmap.agy.common.constant;

/**
 * 认证与权限安全全局常量
 */
public interface AuthConstants {

    /**
     * Redis Access Token 存储前缀
     */
    String TOKEN_PREFIX = "token:";

    /**
     * Redis Refresh Token 存储前缀 (用于 7 天无感刷新)
     */
    String REFRESH_TOKEN_PREFIX = "refresh_token:";

    /**
     * Access Token 访问令牌默认有效期：7200 秒 (2 小时)
     */
    long TOKEN_TTL_SECONDS = 7200L;

    /**
     * Refresh Token 刷新令牌默认有效期：604800 秒 (7 天)
     */
    long REFRESH_TOKEN_TTL_SECONDS = 604800L;

    /**
     * HTTP 鉴权请求头标准名称
     */
    String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Token 协议前缀
     */
    String BEARER_PREFIX = "Bearer ";

    /**
     * 自定义 Token 请求头
     */
    String HEADER_TOKEN = "token";

    /**
     * Request 属性中存放的经过 Filter 验签的真实登录用户 ID
     */
    String ATTR_LOGIN_USER_ID = "LOGIN_USER_ID";

    /**
     * Request 属性中存放的经过 Filter 验签的真实登录用户名
     */
    String ATTR_LOGIN_USER_NAME = "LOGIN_USER_NAME";

    /**
     * Redis RSA 公钥缓存 Key (Plan B: 零配置 Redis 密钥托管)
     */
    String RSA_PUBLIC_KEY_REDIS_KEY = "sys:rsa:public_key";

    /**
     * Redis RSA 私钥缓存 Key (Plan B: 零配置 Redis 密钥托管)
     */
    String RSA_PRIVATE_KEY_REDIS_KEY = "sys:rsa:private_key";

    /**
     * RSA 密钥对在 Redis 中的默认托管有效期：30 天 (秒)
     */
    long RSA_KEY_TTL_SECONDS = 2592000L;

    /**
     * Redis 用户权限标识列表缓存前缀 (用于后端 RBAC 强鉴权拦截)
     */
    String TOKEN_PERMS_PREFIX = "token:perms:";

    /**
     * 超级管理员通配符最高权限标识
     */
    String SUPER_ADMIN_PERMISSION = "*:*:*";

    /**
     * 登录失败计数 Redis 前缀
     */
    String LOGIN_FAIL_PREFIX = "login:fail:";

    /**
     * 账号登录锁定 Redis 前缀
     */
    String LOGIN_LOCK_PREFIX = "login:lock:";

    /**
     * 登录 IP 限流 Redis 前缀
     */
    String LOGIN_LIMIT_IP_PREFIX = "login:limit:ip:";

    /**
     * 密码连续错误最大允许次数 (达到后锁定 15 分钟)
     */
    int MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 账号锁定时间：15 分钟 (秒)
     */
    long LOGIN_LOCK_TTL_SECONDS = 900L;

    /**
     * 单个 IP 每分钟最大登录尝试次数
     */
    int MAX_IP_LOGIN_PER_MINUTE = 30;
}
