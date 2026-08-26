package com.greatmap.agy.common.utils;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Jasypt 配置文件敏感属性 ENC(...) 加解密统一工具类
 * 统一采用 PooledPBEStringEncryptor + PBEWithMD5AndDES 算法规范，消除算法漂移。
 */
public final class JasyptEncryptorUtil {

    private JasyptEncryptorUtil() {
    }

    /**
     * 从环境变量 JASYPT_ENCRYPTOR_PASSWORD 或系统属性 jasypt.encryptor.password 获取主密钥
     */
    public static String getMasterKey() {
        String envKey = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        String propKey = System.getProperty("jasypt.encryptor.password");
        if (propKey != null && !propKey.trim().isEmpty()) {
            return propKey.trim();
        }
        return "";
    }

    /**
     * 创建基于 PBEWithMD5AndDES 的统一 Jasypt 字符串加密器
     *
     * @param masterKey 主密钥 (禁止为空)
     * @return PooledPBEStringEncryptor
     */
    public static PooledPBEStringEncryptor createEncryptor(String masterKey) {
        if (masterKey == null || masterKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "【Jasypt 安全拒绝】未配置 Jasypt 主密钥！请通过环境变量 JASYPT_ENCRYPTOR_PASSWORD 或属性 jasypt.encryptor.password 传入主密钥。"
            );
        }
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(masterKey.trim());
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
        return encrypt(plainText, getRequiredMasterKey());
    }

    public static String encrypt(String plainText, String masterKey) {
        if (plainText == null) {
            return "";
        }
        PooledPBEStringEncryptor encryptor = createEncryptor(masterKey);
        return encryptor.encrypt(plainText);
    }

    public static String encryptToEnc(String plainText) {
        return encryptToEnc(plainText, getRequiredMasterKey());
    }

    public static String encryptToEnc(String plainText, String masterKey) {
        return "ENC(" + encrypt(plainText, masterKey) + ")";
    }

    public static String decrypt(String cipherText) {
        return decrypt(cipherText, getRequiredMasterKey());
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

    private static String getRequiredMasterKey() {
        String key = getMasterKey();
        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "【Jasypt 安全拒绝】未检测到环境变量 JASYPT_ENCRYPTOR_PASSWORD 或属性 jasypt.encryptor.password，拒绝使用空密钥执行加解密！"
            );
        }
        return key;
    }

    public static void main(String[] args) {
        String masterKey = getMasterKey();
        if (masterKey.isEmpty()) {
            System.err.println("【提示】请先设置环境变量 JASYPT_ENCRYPTOR_PASSWORD 或传入密钥后执行！");
            return;
        }
        System.out.println("====== Jasypt 配置文件 ENC(...) 密文生成示例 ======");
        System.out.println("明文: postgres  =>  密文: " + encryptToEnc("postgres", masterKey));
        System.out.println("明文: 123456    =>  密文: " + encryptToEnc("123456", masterKey));
    }
}
