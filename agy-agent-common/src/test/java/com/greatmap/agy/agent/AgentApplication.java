package com.greatmap.agy.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * AGY 智能体通用底座本地测试入口
 */
@SpringBootApplication(
        scanBasePackages = {"com.greatmap.agy.agent"},
        exclude = {
                DataSourceAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        },
        excludeName = {
                "com.greatmap.agy.common.config.MyBatisPlusConfig",
                "com.greatmap.agy.common.config.SecurityConfig"
        }
)
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
