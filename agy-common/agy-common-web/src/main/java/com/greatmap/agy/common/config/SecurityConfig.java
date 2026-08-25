package com.greatmap.agy.common.config;

import cn.hutool.json.JSONUtil;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.exception.ErrorCode;
import com.greatmap.agy.common.security.TokenAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security 全局无状态与 Token 鉴权过滤器配置
 * 开启 @EnableMethodSecurity 支持方法级 @PreAuthorize("@ss.hasPerm('...')") 强鉴权拦截
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private SecurityProperties securityProperties;

    @Bean
    public TokenAuthFilter tokenAuthFilter() {
        return new TokenAuthFilter(stringRedisTemplate, securityProperties);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    Result<Void> errorResult = Result.error(ErrorCode.FORBIDDEN.getCode(), "抱歉，您没有操作该资源的权限，请联系管理员分配");
                    response.getWriter().write(JSONUtil.toJsonStr(errorResult));
                })
            )
            .addFilterBefore(tokenAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
