package com.greatmap.agy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = AgyUpmsApplication.class)
@ActiveProfiles("dev")
@DisplayName("Jasypt 属性绑定与 SpringBoot 容器启动集成测试")
public class JasyptConfigIntegrationTest {

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("验证 Spring 容器已将 ENC(...) 自动解密为真实口令明文")
    public void testDecryptedProperties() {
        String dbUser = environment.getProperty("spring.datasource.username");
        String dbPass = environment.getProperty("spring.datasource.password");
        String redisPass = environment.getProperty("spring.data.redis.password");

        assertEquals("postgres", dbUser, "PostgreSQL 用户名应为 postgres");
        assertEquals("postgres", dbPass, "PostgreSQL 密码应为 postgres");
    }
}
