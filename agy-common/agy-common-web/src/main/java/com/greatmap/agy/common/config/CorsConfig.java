package com.greatmap.agy.common.config;

import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局 CORS 跨域配置 (支持 Spring Security 与 Spring MVC 全链路跨域穿透)
 * 支持在 application.yml / application-prod.yml 中通过 framework.security.cors 配置精准来源白名单
 */
@Configuration
public class CorsConfig {

    @Autowired(required = false)
    private SecurityProperties securityProperties;

    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        SecurityProperties.Cors cors = (securityProperties != null && securityProperties.getCors() != null)
                ? securityProperties.getCors()
                : new SecurityProperties.Cors();

        config.setAllowCredentials(cors.isAllowCredentials());

        // 1. 设置来源模式白名单 (默认支持本地 localhost / 127.0.0.1，生产环境严格指定具体域名)
        if (CollUtil.isNotEmpty(cors.getAllowedOriginPatterns())) {
            config.setAllowedOriginPatterns(cors.getAllowedOriginPatterns());
        } else {
            config.addAllowedOriginPattern("http://localhost:*");
            config.addAllowedOriginPattern("http://127.0.0.1:*");
        }

        // 2. 设置允许的 HTTP 方法
        if (CollUtil.isNotEmpty(cors.getAllowedMethods())) {
            config.setAllowedMethods(cors.getAllowedMethods());
        } else {
            config.addAllowedMethod("*");
        }

        // 3. 设置允许的请求头与暴露响应头
        if (CollUtil.isNotEmpty(cors.getAllowedHeaders())) {
            config.setAllowedHeaders(cors.getAllowedHeaders());
        } else {
            config.addAllowedHeader("*");
        }
        if (CollUtil.isNotEmpty(cors.getExposedHeaders())) {
            config.setExposedHeaders(cors.getExposedHeaders());
        }

        config.setMaxAge(cors.getMaxAge() > 0 ? cors.getMaxAge() : 3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @ConditionalOnMissingBean(name = "corsFilterRegistration")
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
