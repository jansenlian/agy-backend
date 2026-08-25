package com.greatmap.agy.common.utils;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Jasypt 配置文件敏感属性 ENC(...) 加解密统一工具类
 * 统一采用 PooledPBEStringEncryptor + PBEWithMD5AndDES 算法规范，消除算法漂移。
 */
public final class JasyptEncryptorUtil {

    public static final String DEFAULT_MASTER_KEY = "AGY_FRAMEWORK_MASTER_SECRET_KEY_2026";

    private JasyptEncryptorUtil() {
    }

    public static PooledPBEStringEncryptor createEncryptor(String masterKey) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(masterKey != null && !masterKey.trim().isEmpty() ? masterKey : DEFAULT_MASTER_KEY);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGenerator(new org.jasypt.salt.RandomSaltGenerator());
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

    public static String encrypt(String plainText) {
        return encrypt(plainText, DEFAULT_MASTER_KEY);
    }

    public static String encrypt(String plainText, String masterKey) {
        if (plainText == null) {
            return "";
        }
        PooledPBEStringEncryptor encryptor = createEncryptor(masterKey);
        return encryptor.encrypt(plainText);
    }

    public static String encryptToEnc(String plainText) {
        return encryptToEnc(plainText, DEFAULT_MASTER_KEY);
    }

    public static String encryptToEnc(String plainText, String masterKey) {
        return "ENC(" + encrypt(plainText, masterKey) + ")";
    }

    public static String decrypt(String cipherText) {
        return decrypt(cipherText, DEFAULT_MASTER_KEY);
    }

    public static String decrypt(String cipherText, String masterKey) {
        if (cipherText == null || cipherText.trim().isEmpty()) {
            return "";
        }
        String cleanText = cipherText.trim();
        if (cleanText.startsWith("ENC(") && cleanText.endsWith(")")) {
            cleanText = cleanText.substring(4, cleanText.length() - 1);
        }
        PooledPBEStringEncryptor encryptor = createEncryptor(masterKey);
        return encryptor.decrypt(cleanText);
    }

    public static void main(String[] args) {
        String masterKey = DEFAULT_MASTER_KEY;
        System.out.println("====== Jasypt 配置文件 ENC(...) 密文生成示例 ======");
        System.out.println("明文: postgres  =>  密文: " + encryptToEnc("postgres", masterKey));
        System.out.println("明文: 123456    =>  密文: " + encryptToEnc("123456", masterKey));
    }
}
