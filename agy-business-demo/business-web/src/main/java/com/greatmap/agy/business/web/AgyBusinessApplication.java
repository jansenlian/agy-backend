package com.greatmap.agy.business.web;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 业务系统主启动类 (平时在 IDEA 里唯一运行的项目入口)
 */
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.greatmap.agy")
@MapperScan("com.greatmap.agy.business.service.mapper")
public class AgyBusinessApplication {

    private static final Logger log = LoggerFactory.getLogger(AgyBusinessApplication.class);

    public static void main(String[] args) {
        org.springframework.context.ConfigurableApplicationContext application = SpringApplication.run(AgyBusinessApplication.class, args);
        org.springframework.core.env.Environment env = application.getEnvironment();
        String ip = "localhost";
        try {
            ip = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException ignored) {
        }
        String port = env.getProperty("server.port", "9201");
        String path = env.getProperty("server.servlet.context-path", "");
        if ("/".equals(path)) {
            path = "";
        }

        log.info("\n----------------------------------------------------------\n\t" +
                "🚀 业务系统服务 (business-server) 启动成功！\n\t" +
                "🏠 本地访问地址: \thttp://localhost:" + port + path + "\n\t" +
                "🌐 外部访问地址: \thttp://" + ip + ":" + port + path + "\n\t" +
                "📄 Swagger 接口文档: \thttp://localhost:" + port + path + "/swagger-ui/index.html\n\t" +
                "📋 OpenAPI JSON: \thttp://localhost:" + port + path + "/v3/api-docs\n" +
                "----------------------------------------------------------");
    }
}
