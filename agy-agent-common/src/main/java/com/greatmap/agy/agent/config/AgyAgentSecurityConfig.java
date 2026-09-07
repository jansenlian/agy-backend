package com.greatmap.agy.agent.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * AGY 智能体通用安全放行配置 (默认禁用，遵循 Fail-Closed 安全防线)
 * 仅当配置项 agy.agent.security.permit-all=true 时才开启免登录匿名放行
 */
@AutoConfiguration
@ConditionalOnClass(EnableWebSecurity.class)
@ConditionalOnProperty(name = "agy.agent.security.permit-all", havingValue = "true", matchIfMissing = false)
@EnableWebSecurity
public class AgyAgentSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain agentSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/agent/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
