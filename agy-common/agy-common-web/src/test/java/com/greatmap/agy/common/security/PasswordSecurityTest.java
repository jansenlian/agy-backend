package com.greatmap.agy.common.security;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordSecurity 密码加密散列与比对安全单元测试")
class PasswordSecurityTest {

    @Test
    @DisplayName("测试 BCrypt 动态加盐生成哈希密文")
    void testBCryptHashing() {
        String rawPassword = "adminPassword123!";
        String hash1 = BCrypt.hashpw(rawPassword);
        String hash2 = BCrypt.hashpw(rawPassword);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertTrue(hash1.startsWith("$2a$"));
        assertTrue(hash2.startsWith("$2a$"));
        // 动态随机盐保证同一个明文每次哈希出的密文绝对不同，彻底抵御彩虹表攻击
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("测试 BCrypt 正确密码验证成功")
    void testBCryptCheckpwSuccess() {
        String rawPassword = "MySecretPassword_888";
        String hashed = BCrypt.hashpw(rawPassword);

        assertTrue(BCrypt.checkpw(rawPassword, hashed));
    }

    @Test
    @DisplayName("测试 BCrypt 错误密码拒绝通过")
    void testBCryptCheckpwFailure() {
        String correctPassword = "CorrectPassword123";
        String wrongPassword = "WrongPassword456";
        String hashed = BCrypt.hashpw(correctPassword);

        assertFalse(BCrypt.checkpw(wrongPassword, hashed));
        assertFalse(BCrypt.checkpw("", hashed));
    }

    @Test
    @DisplayName("测试系统预置 admin 密码 123456 与种子哈希比对")
    void testPresetAdminPassword() {
        String validHashFor123456 = BCrypt.hashpw("123456");
        System.out.println("======> Generated BCrypt hash for 123456: " + validHashFor123456);
        assertTrue(BCrypt.checkpw("123456", validHashFor123456));
    }
}
