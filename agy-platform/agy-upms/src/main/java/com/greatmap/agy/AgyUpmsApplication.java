package com.greatmap.agy;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 平台唯一主运行入口类 (包含 gateway, auth, upms 全套底座功能，端口 9200)
 */
@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.greatmap.agy"})
@MapperScan("com.greatmap.agy.**.mapper")
public class AgyUpmsApplication {

    private static final Logger log = LoggerFactory.getLogger(AgyUpmsApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(AgyUpmsApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = "localhost";
        try {
            ip = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException ignored) {
        }
        String port = env.getProperty("server.port", "9200");
        String path = env.getProperty("server.servlet.context-path", "");
        if ("/".equals(path)) {
            path = "";
        }

        log.info("\n----------------------------------------------------------\n\t" +
                "👥 平台基座聚合服务 (agy-platform) 启动成功！\n\t" +
                "🏠 本地访问地址: \thttp://localhost:" + port + path + "\n\t" +
                "🌐 外部访问地址: \thttp://" + ip + ":" + port + path + "\n\t" +
                "📄 Swagger 接口文档: \thttp://localhost:" + port + path + "/swagger-ui/index.html\n\t" +
                "📋 OpenAPI JSON: \thttp://localhost:" + port + path + "/v3/api-docs\n" +
                "----------------------------------------------------------");
    }
}
