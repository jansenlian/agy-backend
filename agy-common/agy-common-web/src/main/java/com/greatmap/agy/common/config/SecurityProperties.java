package com.greatmap.agy.common.config;

import com.greatmap.agy.common.utils.RsaUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全认证全局可配置属性 (支持在 application.yml 中随时调整 Token 有效期与 RSA 密钥)
 */
@Data
@Component
@ConfigurationProperties(prefix = "framework.security")
public class SecurityProperties {

    /**
     * Token 令牌有效期配置
     */
    private Token token = new Token();

    /**
     * RSA 密码传输加解密密钥配置
     */
    private Rsa rsa = new Rsa();

    /**
     * CORS 跨域白名单与安全属性配置
     */
    private Cors cors = new Cors();

    @PostConstruct
    public void init() {
        if (rsa != null && (rsa.getPrivateKey() != null || rsa.getPublicKey() != null)) {
            RsaUtils.initKeys(rsa.getPrivateKey(), rsa.getPublicKey());
        }
    }

    @Data
    public static class Token {
        /**
         * 访问令牌有效时长 (秒)，默认 7200 秒 (2小时)
         */
        private long accessTokenTtl = 7200L;

        /**
         * 刷新令牌有效时长 (秒)，默认 604800 秒 (7天)
         */
        private long refreshTokenTtl = 604800L;

        /**
         * 绝对最大会话存活时长 (秒)，默认 86400 秒 (24小时，杜绝无限滑动续期逃逸)
         */
        private long maxSessionTtl = 86400L;
    }

    @Data
    public static class Rsa {
        /**
         * RSA 2048 位公钥 (Base64 PKCS#8) - 若未配置则使用默认内置公钥
         */
        private String publicKey;

        /**
         * RSA 2048 位私钥 (Base64 PKCS#8) - 若未配置则使用默认内置私钥
         */
        private String privateKey;
    }

    @Data
    public static class Cors {
        /**
         * 是否开启凭据携带 (Access-Control-Allow-Credentials)，默认 true
         */
        private boolean allowCredentials = true;

        /**
         * 允许的跨域来源模式白名单 (建议生产环境通过 application-prod.yml 严格指定具体域名，如 https://*.yourdomain.com)
         * 默认允许本地开发端口: http://localhost:*, http://127.0.0.1:*
         */
        private java.util.List<String> allowedOriginPatterns = java.util.Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*"
        );

        /**
         * 允许的 HTTP 请求方法
         */
        private java.util.List<String> allowedMethods = java.util.Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        );

        /**
         * 允许的 HTTP 请求头
         */
        private java.util.List<String> allowedHeaders = java.util.Collections.singletonList("*");

        /**
         * 暴露给前端的响应头
         */
        private java.util.List<String> exposedHeaders = java.util.Arrays.asList(
                "X-Trace-Id", "Authorization"
        );

        /**
         * 预检请求缓存有效时间 (秒)，默认 3600 秒 (1小时)
         */
        private long maxAge = 3600L;
    }
}
