package com.greatmap.agy.common.config;

import com.greatmap.agy.common.utils.JasyptEncryptorUtil;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * AGY 全局统一 Jasypt 默认环境后置处理器
 * 实现全域微服务【单一源点】密钥管理：
 * 1. 优先读取系统环境变量 JASYPT_ENCRYPTOR_PASSWORD 或命令行参数
 * 2. 若未配置，则自动保底使用 agy-common 唯一的 DEFAULT_MASTER_KEY
 * 3. 从而使所有业务微服务的 application.yml 完全无需重复声明 jasypt 配置！
 */
public class AgyJasyptEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String envKey = environment.getProperty("JASYPT_ENCRYPTOR_PASSWORD");
        String configKey = environment.getProperty("jasypt.encryptor.password");

        String masterKey = (envKey != null && !envKey.isBlank()) ? envKey :
                ((configKey != null && !configKey.isBlank()) ? configKey : JasyptEncryptorUtil.DEFAULT_MASTER_KEY);

        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("jasypt.encryptor.password", masterKey);
        defaultProperties.put("jasypt.encryptor.algorithm", "PBEWithMD5AndDES");
        defaultProperties.put("jasypt.encryptor.iv-generator-classname", "org.jasypt.iv.NoIvGenerator");
        defaultProperties.put("jasypt.encryptor.iv-generator-class-name", "org.jasypt.iv.NoIvGenerator");
        defaultProperties.put("jasypt.encryptor.salt-generator-class-name", "org.jasypt.salt.RandomSaltGenerator");

        // 使用 addLast 保底优先级，外部显式传入的优先
        environment.getPropertySources().addLast(new MapPropertySource("agyJasyptDefaultProperties", defaultProperties));
    }

    @Override
    public int getOrder() {
        // 在 Jasypt 转换 PropertySource 之前优先就绪
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
