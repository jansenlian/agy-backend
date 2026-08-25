package com.greatmap.agy.common.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("BusinessArchitectureVerifier 架构校验器单元测试")
class BusinessArchitectureVerifierTest {

    @Test
    @DisplayName("验证在 common 基础库环境中不会误命中 UPMS 内部类")
    void testVerifyNoUpmsDuplicateCode() {
        assertDoesNotThrow(BusinessArchitectureVerifier::verifyNoUpmsDuplicateCode,
                "common 基础库中绝不包含 UPMS Web 控制器与服务实现类");
    }
}
