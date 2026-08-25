package com.greatmap.agy.auth.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.greatmap.agy.common.utils.RsaUtils;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

/**
 * OAuth 2.1 授权服务器标准配置 (生产级)
 * 1. 采用 JdbcRegisteredClientRepository 基于数据库表 oauth2_registered_client 进行持久化在线管理；
 * 2. 默认初始化一条内部种子客户端 agy-client (密码采用 BCrypt 强哈希散列)；
 * 3. 签名公私钥与 RsaUtils (Redis 托管密钥对) 统一，服务重启签名 KeyID 保持稳定，杜绝 Token 失效；
 * 4. Issuer 支持通过 application.yml 中的 framework.security.oauth2.issuer 动态配置，缺省时自动根据请求推导。
 */
@Slf4j
@Configuration
@ConditionalOnClass(RegisteredClientRepository.class)
public class AuthorizationServerConfig {

    public static final String AUTH_KEY_ID = "agy-auth-key-v1";

    @Value("${framework.security.oauth2.issuer:}")
    private String issuer;

    @Value("${framework.security.oauth2.client-id:agy-client}")
    private String clientId;

    @Value("${framework.security.oauth2.client-secret:${SYSTEM_OAUTH2_SECRET:agy-secret-123456}}")
    private String clientSecret;

    @Value("${framework.security.oauth2.redirect-uri:http://127.0.0.1:5006/login}")
    private String redirectUri;

    @Bean
    @ConditionalOnMissingBean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

        // 检查数据库中是否已存在当前配置的客户端，若不存在则自动插入种子客户端记录
        try {
            if (repository.findByClientId(clientId) == null) {
                String encodedSecret = clientSecret.startsWith("{bcrypt}") ? clientSecret : "{bcrypt}" + BCrypt.hashpw(clientSecret);
                RegisteredClient internalClient = RegisteredClient.withId("agy-client-id-01")
                        .clientId(clientId)
                        .clientSecret(encodedSecret)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .redirectUri(redirectUri)
                        .scope(OidcScopes.OPENID)
                        .scope(OidcScopes.PROFILE)
                        .scope("read")
                        .scope("write")
                        .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(false)
                                .requireProofKey(true)
                                .build())
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(2))
                                .refreshTokenTimeToLive(Duration.ofDays(7))
                                .reuseRefreshTokens(false)
                                .build())
                        .build();
                repository.save(internalClient);
                log.info("OAuth 2.1 数据库初始化：已成功注册配置客户端 [{}] (配置回调地址: {})", clientId, redirectUri);
            }
        } catch (Exception e) {
            log.warn("OAuth 2.1 初始化配置客户端检查跳过 (可能数据库表尚未创建或已存在): {}", e.getMessage());
        }

        return repository;
    }

    @Bean
    @ConditionalOnMissingBean
    public JWKSource<SecurityContext> jwkSource() {
        // 🌟 统一复用 RsaUtils (从 Redis 托管获取的稳定 2048 位密钥对)
        RSAPublicKey publicKey = RsaUtils.getPublicKey();
        RSAPrivateKey privateKey = RsaUtils.getPrivateKey();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(AUTH_KEY_ID)
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationServerSettings authorizationServerSettings() {
        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
        if (StrUtil.isNotBlank(issuer)) {
            builder.issuer(issuer.trim());
            log.info("OAuth 2.1 授权服务器 Issuer 配置为: {}", issuer.trim());
        }
        return builder.build();
    }
}
