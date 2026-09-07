package com.greatmap.agy.hr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AGY 智能人事与运维资产 360° 全景问答微服务启动入口
 */
@SpringBootApplication(
        scanBasePackages = {"com.greatmap.agy"},
        exclude = {com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class}
)
@MapperScan("com.greatmap.agy.hr.mapper")
public class AgyHrAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgyHrAgentApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("  AGY 智能人事与运维资产 Agent (Java 25 + Spring Boot 4.1.0) 启动成功！");
        System.out.println("  1. 智能问答接口: POST http://localhost:8090/api/agent/chat");
        System.out.println("  2. SSE 打字机流: POST http://localhost:8090/api/agent/chat/stream");
        System.out.println("==================================================================");
    }
}
