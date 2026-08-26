package com.greatmap.agy.common;

import com.greatmap.agy.common.utils.JasyptEncryptorUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jasypt 配置文件 ENC(...) 属性密文加解密单元测试")
public class JasyptEncryptorUtilTest {

    @Test
    @DisplayName("验证自定义密钥加密后均可精确解密还原")
    public void testEncryptAndDecryptWithCustomKey() {
        String testKey = "TEST_SECRET_KEY_FOR_UNIT_TEST_2026";
        String rawDbUser = "sample_user";
        String rawDbPass = "Sample_pass_2026!";

        String encryptedUser = JasyptEncryptorUtil.encrypt(rawDbUser, testKey);
        String encryptedPass = JasyptEncryptorUtil.encrypt(rawDbPass, testKey);

        assertNotNull(encryptedUser);
        assertNotNull(encryptedPass);
        assertNotEquals(rawDbUser, encryptedUser);
        assertNotEquals(rawDbPass, encryptedPass);

        String decryptedUser = JasyptEncryptorUtil.decrypt(encryptedUser, testKey);
        String decryptedPass = JasyptEncryptorUtil.decrypt(encryptedPass, testKey);

        assertEquals(rawDbUser, decryptedUser);
        assertEquals(rawDbPass, decryptedPass);

        // 验证 ENC(...) 包装格式自动解密
        String encWrapped = JasyptEncryptorUtil.encryptToEnc(rawDbPass, testKey);
        assertTrue(encWrapped.startsWith("ENC(") && encWrapped.endsWith(")"));
        assertEquals(rawDbPass, JasyptEncryptorUtil.decrypt(encWrapped, testKey));
    }

    @Test
    @DisplayName("验证未提供密钥时触发 Fail-Fast 安全拒绝")
    public void testFailFastWhenNoKeyProvided() {
        assertThrows(IllegalArgumentException.class, () -> JasyptEncryptorUtil.createEncryptor(null));
        assertThrows(IllegalArgumentException.class, () -> JasyptEncryptorUtil.createEncryptor("   "));
    }
}
