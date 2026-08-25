package com.greatmap.agy.common.aspect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LogAspect 操作日志切面与敏感数据统一脱敏单元测试")
class LogAspectTest {

    @Test
    @DisplayName("测试密码与敏感凭证字段自动脱敏为 ******")
    void testMaskSensitiveDataPasswordsAndTokens() {
        String json = "{\"username\":\"admin\",\"password\":\"123456\",\"oldPassword\":\"abc\",\"newPassword\":\"def\",\"token\":\"eyJhbGciOi...\",\"phoneNumber\":\"13800138000\"}";
        String masked = LogAspect.maskSensitiveData(json);

        assertTrue(masked.contains("\"password\":\"******\""));
        assertTrue(masked.contains("\"oldPassword\":\"******\""));
        assertTrue(masked.contains("\"newPassword\":\"******\""));
        assertTrue(masked.contains("\"token\":\"******\""));
        assertTrue(masked.contains("\"phoneNumber\":\"******\""));
        assertTrue(masked.contains("\"username\":\"admin\""));

        assertFalse(masked.contains("123456"));
        assertFalse(masked.contains("13800138000"));
    }

    @Test
    @DisplayName("测试非敏感字段保持原样不被误伤")
    void testMaskSensitiveDataNonSensitiveUnchanged() {
        String json = "{\"id\":1001,\"title\":\"用户管理\",\"status\":1,\"deptName\":\"研发部\"}";
        String masked = LogAspect.maskSensitiveData(json);

        assertEquals(json, masked);
    }
}
