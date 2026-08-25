package com.greatmap.agy.auth.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("OAuth 2.1 授权服务器配置与客户端仓库单元测试")
class AuthorizationServerConfigTest {

    @Test
    @DisplayName("测试 JWKSource 提供稳定 KeyID 和有效 2048 位 RSA 公私钥")
    void testJwkSourceStability() throws Exception {
        AuthorizationServerConfig config = new AuthorizationServerConfig();
        JWKSource<SecurityContext> jwkSource = config.jwkSource();
        assertNotNull(jwkSource);

        List<JWK> jwks = jwkSource.get(new JWKSelector(new com.nimbusds.jose.jwk.JWKMatcher.Builder().build()), null);
        assertFalse(jwks.isEmpty());
        JWK jwk = jwks.get(0);

        assertEquals("agy-auth-key-v1", jwk.getKeyID());
        assertEquals("RSA", jwk.getKeyType().getValue());
    }

    @Test
    @DisplayName("测试 Issuer 缺省与显式自定义配置生效机制")
    void testAuthorizationServerSettings() {
        AuthorizationServerConfig configDefault = new AuthorizationServerConfig();
        AuthorizationServerSettings settingsDefault = configDefault.authorizationServerSettings();
        assertNotNull(settingsDefault);
        assertNull(settingsDefault.getIssuer(), "默认未配置时应留空让框架动态解析请求 Host/Header");

        AuthorizationServerConfig configCustom = new AuthorizationServerConfig();
        ReflectionTestUtils.setField(configCustom, "issuer", "https://auth.greatmap.com.cn");
        AuthorizationServerSettings settingsCustom = configCustom.authorizationServerSettings();
        assertNotNull(settingsCustom);
        assertEquals("https://auth.greatmap.com.cn", settingsCustom.getIssuer());
    }

    @Test
    @DisplayName("测试 RegisteredClientRepository 实例化为标准 JdbcRegisteredClientRepository")
    void testRegisteredClientRepository() {
        AuthorizationServerConfig config = new AuthorizationServerConfig();
        JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
        RegisteredClientRepository repository = config.registeredClientRepository(mockJdbcTemplate);

        assertNotNull(repository);
        assertInstanceOf(JdbcRegisteredClientRepository.class, repository);
    }
}
