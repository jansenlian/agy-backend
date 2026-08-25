package com.greatmap.agy.common;

import com.greatmap.agy.common.utils.JasyptEncryptorUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jasypt 配置文件 ENC(...) 属性密文加解密单元测试")
public class JasyptEncryptorUtilTest {

    @Test
    @DisplayName("验证数据库与 Redis 口令加密后均可精确解密还原")
    public void testEncryptAndDecrypt() {
        String rawDbUser = "sample_user";
        String rawDbPass = "Sample_pass_2026!";

        String encryptedUser = JasyptEncryptorUtil.encrypt(rawDbUser);
        String encryptedPass = JasyptEncryptorUtil.encrypt(rawDbPass);

        assertNotNull(encryptedUser);
        assertNotNull(encryptedPass);
        assertNotEquals(rawDbUser, encryptedUser);
        assertNotEquals(rawDbPass, encryptedPass);

        String decryptedUser = JasyptEncryptorUtil.decrypt(encryptedUser);
        String decryptedPass = JasyptEncryptorUtil.decrypt(encryptedPass);

        System.out.println(">>> postgres ENC: ENC(" + encryptedUser + ")");
        System.out.println(">>> 123456 ENC: ENC(" + encryptedPass + ")");

        assertEquals(rawDbUser, decryptedUser);
        assertEquals(rawDbPass, decryptedPass);

        // 验证 ENC(...) 包装格式自动解密
        String encWrapped = JasyptEncryptorUtil.encryptToEnc(rawDbPass);
        assertTrue(encWrapped.startsWith("ENC(") && encWrapped.endsWith(")"));
        assertEquals(rawDbPass, JasyptEncryptorUtil.decrypt(encWrapped));

        // 验证自定义密钥加解密
        String customKey = "MY_CUSTOM_SECRET_KEY_999";
        String customEnc = JasyptEncryptorUtil.encrypt(rawDbPass, customKey);
        assertEquals(rawDbPass, JasyptEncryptorUtil.decrypt(customEnc, customKey));
    }
}
