package com.greatmap.agy.common.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.spec.MGF1ParameterSpec;

/**
 * RSA 非对称加解密工具类 (用于前端登录密码端到端传输加密与防抓包)
 * 支持在 application.yml 中自定义配置密钥，支持 Web Crypto 原生 RSA-OAEP-SHA256 (MGF1-SHA256 / MGF1-SHA1)、PKCS1Padding 以及智能明文回退兼容
 */
@Slf4j
public class RsaUtils {

    private static volatile RSA rsaInstance;

    static {
        try {
            rsaInstance = new RSA();
        } catch (Exception e) {
            log.error("生成默认 RSA 密钥对异常: {}", e.getMessage());
        }
    }

    /**
     * 动态初始化/热更新 RSA 密钥对（从 Spring Boot application.yml 或 Redis 注入）
     */
    public static synchronized void initKeys(String privateKeyBase64, String publicKeyBase64) {
        if (StrUtil.isNotBlank(privateKeyBase64) && StrUtil.isNotBlank(publicKeyBase64)) {
            try {
                rsaInstance = new RSA(privateKeyBase64.trim(), publicKeyBase64.trim());
                log.info("RSA 加密密钥对已成功加载与初始化");
            } catch (Exception e) {
                log.error("加载自定义 RSA 密钥失败，维持保底密钥: {}", e.getMessage());
            }
        }
    }

    /**
     * 获取 Base64 编码的 RSA 公钥 (给前端加密用)
     */
    public static String getPublicKeyBase64() {
        return rsaInstance.getPublicKeyBase64();
    }

    /**
     * 获取 Base64 编码的 RSA 私钥
     */
    public static String getPrivateKeyBase64() {
        return rsaInstance.getPrivateKeyBase64();
    }

    /**
     * 获取原生 RSAPublicKey 对象 (供 OAuth 2.1 JWKSource 构建 JWKS)
     */
    public static java.security.interfaces.RSAPublicKey getPublicKey() {
        return (java.security.interfaces.RSAPublicKey) rsaInstance.getPublicKey();
    }

    /**
     * 获取原生 RSAPrivateKey 对象 (供 OAuth 2.1 JWKSource 构建 JWKS)
     */
    public static java.security.interfaces.RSAPrivateKey getPrivateKey() {
        return (java.security.interfaces.RSAPrivateKey) rsaInstance.getPrivateKey();
    }

    /**
     * RSA 公钥加密
     */
    public static String encrypt(String plainText) {
        if (StrUtil.isBlank(plainText)) {
            return plainText;
        }
        return rsaInstance.encryptBase64(plainText, KeyType.PublicKey);
    }

    /**
     * RSA 私钥解密 (严格 Fail-Closed 故障关闭模式：优先 Web Crypto RSA-OAEP SHA-256 + MGF1-SHA256，其次 PKCS1Padding 与 MGF1-SHA1，解密失败立即抛异常阻断，严禁明文回退)
     */
    public static String decrypt(String cipherText) {
        if (StrUtil.isBlank(cipherText)) {
            throw new com.greatmap.agy.common.exception.BusinessException("密码凭证不能为空");
        }
        if (!Base64.isBase64(cipherText)) {
            throw new com.greatmap.agy.common.exception.BusinessException("密码传输凭证无效或解密失败，请重试");
        }

        byte[] cipherBytes;
        try {
            cipherBytes = Base64.decode(cipherText);
        } catch (Exception e) {
            throw new com.greatmap.agy.common.exception.BusinessException("密码传输凭证损坏，请重试");
        }

        // 1. 尝试 W3C Web Crypto 标准原生 RSA-OAEP (SHA-256 Digest + MGF1-SHA256)
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                    "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
            );
            cipher.init(Cipher.DECRYPT_MODE, rsaInstance.getPrivateKey(), oaepSpec);
            byte[] decrypted = cipher.doFinal(cipherBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e1) {
            // 2. 尝试标准 PKCS1Padding (JSEncrypt / Hutool 默认加密格式)
            try {
                return rsaInstance.decryptStr(cipherText, KeyType.PrivateKey);
            } catch (Exception e2) {
                // 3. 尝试 RSA-OAEP (SHA-256 Digest + MGF1-SHA1)
                try {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
                    OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                            "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT
                    );
                    cipher.init(Cipher.DECRYPT_MODE, rsaInstance.getPrivateKey(), oaepSpec);
                    byte[] decrypted = cipher.doFinal(cipherBytes);
                    return new String(decrypted, StandardCharsets.UTF_8);
                } catch (Exception e3) {
                    // 4. 尝试 RSA-OAEP (SHA-1 Digest + MGF1-SHA1)
                    try {
                        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
                        cipher.init(Cipher.DECRYPT_MODE, rsaInstance.getPrivateKey());
                        byte[] decrypted = cipher.doFinal(cipherBytes);
                        return new String(decrypted, StandardCharsets.UTF_8);
                    } catch (Exception e4) {
                        log.error("RSA 密文解密失败 (拒绝明文降级) [密文长度: {}, 异常类型: {}]", cipherText != null ? cipherText.length() : 0, e1.getClass().getName());
                        throw new com.greatmap.agy.common.exception.BusinessException("密码传输凭证无效或解密失败，请重试");
                    }
                }
            }
        }
    }
}
