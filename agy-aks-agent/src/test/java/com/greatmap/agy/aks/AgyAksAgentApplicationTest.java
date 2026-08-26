package com.greatmap.agy.aks;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("需要连接外部真实数据库联调，离线构建时跳过")
@SpringBootTest(classes = AgyAksAgentApplication.class)
@DisplayName("AKs Agent 联调测试 (依赖外部数据源，默认在无数据库 CI 环境中跳过)")
class AgyAksAgentApplicationTest {

    @Test
    void contextLoads() {
    }
}
