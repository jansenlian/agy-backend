package com.greatmap.agy.common.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.greatmap.agy.common.config.SecurityProperties;
import com.greatmap.agy.common.constant.AuthConstants;
import com.greatmap.agy.common.utils.RsaUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * RSA 密钥对生命周期管理器 (Redis 托管加密存储与自动轮转)
 * 启动时优先从 Redis 读取既有密钥对；私钥经 AES 密文存储，防护密钥从环境变量动态获取；
 */
@Slf4j
@Component
public class RsaKeyManager {

    @Value("${framework.security.rsa.secret-seed:${SYSTEM_RSA_SECRET_SEED:}}")
    private String customAesSeed;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private SecurityProperties securityProperties;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private byte[] getAesSeed() {
        if (StrUtil.isNotBlank(customAesSeed)) {
            return customAesSeed.trim().getBytes(StandardCharsets.UTF_8);
        }
        // 开发/本地测试环境回退为宿主特征推导种子
        String systemIdentifier = System.getProperty("user.name", "agy") + ":" + System.getProperty("os.name", "unknown") + ":" + System.getProperty("user.home", "/tmp");
        return DigestUtil.sha256(systemIdentifier);
    }

    @PostConstruct
    public void init() {
        boolean isProd = "prod".equalsIgnoreCase(activeProfile);

        // 🔴 核心加固：生产环境 (prod) 第一道硬卡点：强校验 SYSTEM_RSA_SECRET_SEED 环境变量，拒绝任何隐式缺省！
        if (isProd && StrUtil.isBlank(customAesSeed)
                && (securityProperties == null || securityProperties.getRsa() == null
                || StrUtil.isBlank(securityProperties.getRsa().getPrivateKey()))) {
            throw new IllegalStateException(
                    "【致命安全拒绝 Fail-Fast】生产环境 (prod) 必须配置环境变量 SYSTEM_RSA_SECRET_SEED 或属性 framework.security.rsa.secret-seed，拒绝降级启动！"
            );
        }

        // 1. 如果 application.yml 中显式配置了静态密钥，优先使用静态配置
        if (securityProperties != null && securityProperties.getRsa() != null
                && StrUtil.isNotBlank(securityProperties.getRsa().getPrivateKey())
                && StrUtil.isNotBlank(securityProperties.getRsa().getPublicKey())) {
            RsaUtils.initKeys(securityProperties.getRsa().getPrivateKey(), securityProperties.getRsa().getPublicKey());
            log.info("【RSA 密钥】优先采用 application.yml 静态配置初始化完成");
            return;
        }

        // 2. 方案 B：Redis 托管密钥对 (AES 动态环境密钥加密私钥，后端重启与多节点无缝复用)
        if (stringRedisTemplate != null) {
            try {
                byte[] seed = getAesSeed();
                String pubKey = stringRedisTemplate.opsForValue().get(AuthConstants.RSA_PUBLIC_KEY_REDIS_KEY);
                String encryptedPrivKey = stringRedisTemplate.opsForValue().get(AuthConstants.RSA_PRIVATE_KEY_REDIS_KEY);

                if (StrUtil.isNotBlank(pubKey) && StrUtil.isNotBlank(encryptedPrivKey)) {
                    SymmetricCrypto aes = SecureUtil.aes(seed);
                    String privKey = aes.decryptStr(encryptedPrivKey);
                    RsaUtils.initKeys(privKey, pubKey);
                    log.info("【RSA 密钥】从 Redis 成功加载既有密钥对 (动态环境变量 AES 解密私钥安全加载完成)");
                    return;
                }

                // Redis 中暂无密钥对，自动生成一套 2048 位密钥并写入 Redis
                RSA rsa = new RSA();
                String newPubKey = rsa.getPublicKeyBase64();
                String rawPrivKey = rsa.getPrivateKeyBase64();

                SymmetricCrypto aes = SecureUtil.aes(seed);
                String encryptedNewPrivKey = aes.encryptBase64(rawPrivKey);

                stringRedisTemplate.opsForValue().set(
                        AuthConstants.RSA_PUBLIC_KEY_REDIS_KEY,
                        newPubKey,
                        Duration.ofSeconds(AuthConstants.RSA_KEY_TTL_SECONDS)
                );
                stringRedisTemplate.opsForValue().set(
                        AuthConstants.RSA_PRIVATE_KEY_REDIS_KEY,
                        encryptedNewPrivKey,
                        Duration.ofSeconds(AuthConstants.RSA_KEY_TTL_SECONDS)
                );

                RsaUtils.initKeys(rawPrivKey, newPubKey);
                log.info("【RSA 密钥】已自动生成全新 2048 位密钥对并经动态 AES 加密存储至 Redis");
                return;
            } catch (Exception e) {
                // 🔴 核心加固：生产环境禁止拦截或吞掉 Fail-Fast 异常，直接抛出阻断容器启动！
                if (isProd) {
                    throw new IllegalStateException("【致命安全拒绝 Fail-Fast】生产环境 (prod) Redis 密钥对加载/解密异常，强行阻断应用启动！", e);
                }
                log.warn("【RSA 密钥】连接 Redis 异常，自动降级为本地内存密钥对: {}", e.getMessage());
            }
        } else if (isProd) {
            throw new IllegalStateException("【致命安全拒绝 Fail-Fast】生产环境 (prod) 未配置可用的 StringRedisTemplate，强行阻断应用启动！");
        }

        // 3. 仅开发/测试环境允许 Redis 不可用时的内存保底
        RSA memoryRsa = new RSA();
        RsaUtils.initKeys(memoryRsa.getPrivateKeyBase64(), memoryRsa.getPublicKeyBase64());
        log.info("【RSA 密钥】非生产环境内存保底密钥对初始化完成");
    }
}
