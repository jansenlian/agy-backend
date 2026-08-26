package com.greatmap.agy.hr;

import com.greatmap.agy.agent.service.AgyAgentService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("需要连接外部真实数据库联调，离线构建时跳过")
@SpringBootTest(classes = AgyHrAgentApplication.class)
@DisplayName("智能人事 Agent 联调测试 (依赖外部数据源，默认在无数据库 CI 环境中跳过)")
class AgyHrAgentApplicationTest {

    @Autowired(required = false)
    private AgyAgentService agyAgentService;

    @Test
    @DisplayName("测试智能体问答功能")
    void testAgentChatWithRealDatabase() {
        if (agyAgentService == null) {
            return;
        }
        String question = "请帮我查询员工技能画像信息";
        String answer = agyAgentService.chat("test_user_01", question);
        assertNotNull(answer);
    }
}
