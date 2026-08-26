package com.greatmap.agy.util.web.controller;

import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JasyptController 统一加解密控制器单元测试")
class JasyptControllerTest {

    @Test
    @DisplayName("测试 JasyptController 加密与解密闭环 (验证与 common 统一加密算法一致)")
    void testEncodeAndDecode() {
        JasyptController controller = new JasyptController();
        ReflectionTestUtils.setField(controller, "defaultPassword", "TEST_SECRET_KEY_FOR_CONTROLLER_2026");

        String rawText = "MySecretDatabasePassword2026";
        Result<String> encodeRes = controller.encode(rawText, null);
        assertNotNull(encodeRes);
        assertEquals(ErrorCode.SUCCESS.getCode(), encodeRes.getCode());
        String cipherText = encodeRes.getData();
        assertNotNull(cipherText);
        assertNotEquals(rawText, cipherText);

        // 测试直接解密
        Result<String> decodeRes = controller.decode(cipherText, null);
        assertEquals(ErrorCode.SUCCESS.getCode(), decodeRes.getCode());
        assertEquals(rawText, decodeRes.getData());

        // 测试带 ENC(...) 包装解密
        Result<String> decodeEncRes = controller.decode("ENC(" + cipherText + ")", null);
        assertEquals(ErrorCode.SUCCESS.getCode(), decodeEncRes.getCode());
        assertEquals(rawText, decodeEncRes.getData());
    }
}
