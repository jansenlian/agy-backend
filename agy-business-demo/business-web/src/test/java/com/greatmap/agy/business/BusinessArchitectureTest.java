package com.greatmap.agy.business;

import com.greatmap.agy.common.test.BusinessArchitectureVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("业务微服务独立性与绝对干净架构边界测试")
class BusinessArchitectureTest {

    @Test
    @DisplayName("验证 agy-business 中绝无 UPMS 重复代码 (全量剥离交由 agy-platform 唯一托管)")
    void verifyNoUpmsDuplicateCodeInBusinessRepo() {
        BusinessArchitectureVerifier.verifyNoUpmsDuplicateCode();
    }
}
