package com.greatmap.agy.agent;

import com.greatmap.agy.agent.core.UniversalAgent;
import com.greatmap.agy.agent.service.AgyAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = AgentApplication.class)
class AgyAgentAutoConfigurationTest {

    @Autowired
    private UniversalAgent universalAgent;

    @Autowired
    private AgyAgentService agyAgentService;

    @Test
    void contextLoads() {
        assertNotNull(universalAgent, "UniversalAgent 动态代理 Bean 应该被自动装配成功");
        assertNotNull(agyAgentService, "AgyAgentService 服务门面 Bean 应该被自动装配成功");
        System.out.println(">>> 【AGY Agent 自动装配测试通过】核心组件已全部正常注册注入！");
    }
}
