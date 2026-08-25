package com.greatmap.agy.common.config;

import com.greatmap.agy.common.utils.JasyptEncryptorUtil;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jasypt 自定义 StringEncryptor 统一配置
 * 确保应用启动时无论环境配置如何，均使用与 JasyptEncryptorUtil 完全一致的加解密算法与 IV 生成器。
 */
@Configuration
public class JasyptConfig {

    @Value("${jasypt.encryptor.password:" + JasyptEncryptorUtil.DEFAULT_MASTER_KEY + "}")
    private String masterKey;

    @Bean("jasyptStringEncryptor")
    @Primary
    public StringEncryptor jasyptStringEncryptor() {
        return JasyptEncryptorUtil.createEncryptor(masterKey);
    }
}
