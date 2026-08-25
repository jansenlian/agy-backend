package com.greatmap.agy.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RsaUtils RSA 端到端非对称加解密工具类测试")
class RsaUtilsTest {

    @Test
    @DisplayName("测试公钥加密与私钥解密完整闭环")
    void testEncryptAndDecrypt() {
        String originalPassword = "MySecretPassword@2026!";
        String cipherText = RsaUtils.encrypt(originalPassword);

        assertNotNull(cipherText);
        assertNotEquals(originalPassword, cipherText);
        assertTrue(cipherText.length() > 50, "密文字符串长度应大于 50");

        String decrypted = RsaUtils.decrypt(cipherText);
        assertEquals(originalPassword, decrypted);
    }

    @Test
    @DisplayName("测试传入非法明文或损坏密文时严格抛出业务异常 (Fail-Closed 故障关闭)")
    void testDecryptPlainTextThrowsFailClosedException() {
        String plainText = "123456";
        assertThrows(com.greatmap.agy.common.exception.BusinessException.class, () ->
                RsaUtils.decrypt(plainText), "传入非法明文时必须严格抛出解密失败异常");

        assertThrows(com.greatmap.agy.common.exception.BusinessException.class, () ->
                RsaUtils.decrypt("invalid-base64-random-text"), "传入损坏密文时必须严格抛出解密失败异常");
    }

    @Test
    @DisplayName("测试获取 RSA Base64 公钥")
    void testGetPublicKey() {
        String publicKey = RsaUtils.getPublicKeyBase64();
        assertNotNull(publicKey);
        assertTrue(publicKey.startsWith("MIIBIj") || publicKey.length() > 100);
    }

    @Test
    @DisplayName("测试 Web Crypto 原生 RSA-OAEP SHA-256 / MGF1-SHA256 兼容解密")
    void testWebCryptoOaepSha256Decrypt() throws Exception {
        String originalText = "123456";

        // 模拟 Web Crypto API 使用 RSA-OAEP-SHA256 / MGF1-SHA256 加密
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/OAEPPadding");
        javax.crypto.spec.OAEPParameterSpec oaepParams = new javax.crypto.spec.OAEPParameterSpec(
                "SHA-256", "MGF1", java.security.spec.MGF1ParameterSpec.SHA256, javax.crypto.spec.PSource.PSpecified.DEFAULT
        );
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, RsaUtils.getPublicKey(), oaepParams);
        byte[] encrypted = cipher.doFinal(originalText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String cipherBase64 = cn.hutool.core.codec.Base64.encode(encrypted);

        String decrypted = RsaUtils.decrypt(cipherBase64);
        assertEquals(originalText, decrypted, "OAEP-SHA256 / MGF1-SHA256 密文应能被正确解密");
    }

    @Test
    @DisplayName("测试 Web Crypto 原生 RSA-OAEP SHA-256 / MGF1-SHA1 兼容解密")
    void testWebCryptoOaepSha256Mgf1Sha1Decrypt() throws Exception {
        String originalText = "123456";

        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/OAEPPadding");
        javax.crypto.spec.OAEPParameterSpec oaepParams = new javax.crypto.spec.OAEPParameterSpec(
                "SHA-256", "MGF1", java.security.spec.MGF1ParameterSpec.SHA1, javax.crypto.spec.PSource.PSpecified.DEFAULT
        );
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, RsaUtils.getPublicKey(), oaepParams);
        byte[] encrypted = cipher.doFinal(originalText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String cipherBase64 = cn.hutool.core.codec.Base64.encode(encrypted);

        String decrypted = RsaUtils.decrypt(cipherBase64);
        assertEquals(originalText, decrypted, "OAEP-SHA256 / MGF1-SHA1 密文应能被正确解密");
    }
}
