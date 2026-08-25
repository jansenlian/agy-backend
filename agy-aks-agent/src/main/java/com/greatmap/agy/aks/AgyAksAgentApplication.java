package com.greatmap.agy.aks;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AGY AKS阿克苏智慧耕地违法违规预警与驾驶舱大盘统计智能体微服务启动入口
 */
@SpringBootApplication(
        scanBasePackages = {"com.greatmap.agy"},
        exclude = {com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class}
)
@MapperScan("com.greatmap.agy.aks.mapper")
public class AgyAksAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgyAksAgentApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("  AGY AKS智慧耕地预警与驾驶舱智能体(Java 25+Spring Boot 4.1.0)启动成功!");
        System.out.println("  1. 智能问答接口: POST http://localhost:8092/api/agent/chat");
        System.out.println("  2. SSE打字机流: GET  http://localhost:8092/api/agent/chat/stream?message=xxx");
        System.out.println("  3. 核心工具集: YjAgentTools(预警诊断) + FarmlandStatAgentTools(大盘统计)");
        System.out.println("==================================================================");
    }
}
