package com.greatmap.agy.hr;

import com.greatmap.agy.agent.service.AgyAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = AgyHrAgentApplication.class)
class AgyHrAgentApplicationTest {

    @Autowired
    private AgyAgentService agyAgentService;

    @Test
    void testAgentChatWithRealDatabase() {
        assertNotNull(agyAgentService, "AgyAgentService 应该成功注入");

        System.out.println("\n------------------------------------------------------------------");
        System.out.println(">>> 【测试 1】自然语言提问员工人事与薪酬数据");
        String question1 = "请帮我查一下王健的职位、毕业院校、底薪天费率以及他参与负责的项目清单";
        System.out.println("用户提问: " + question1);
        String answer1 = agyAgentService.chat("test_user_01", question1);
        System.out.println("\nAgent 真实智能回答:\n" + answer1);
        assertTrue(answer1.contains("王健") || answer1.contains("研发"), "回答中应包含王健的真实档案信息");

        System.out.println("\n------------------------------------------------------------------");
        System.out.println(">>> 【测试 2】自然语言提问测试环境服务器资产");
        String question2 = "查一下阿克苏项目的测试环境服务器 IP 和 Swagger 地址";
        System.out.println("用户提问: " + question2);
        String answer2 = agyAgentService.chat("test_user_01", question2);
        System.out.println("\nAgent 真实智能回答:\n" + answer2);
        assertTrue(answer2.contains("192.168.11") || answer2.contains("阿克苏"), "回答中应包含阿克苏服务器信息");
        System.out.println("------------------------------------------------------------------\n");

        System.out.println(">>> 【测试 3】验证 SSE 流式打字机 TokenStream 响应");
        var emitter = agyAgentService.chatStream("test_user_01", "周绍涛总监的职称和专业是什么？");
        assertNotNull(emitter, "SseEmitter 流式通道不应为空");
        System.out.println("SseEmitter 流式通道创建成功！\n");
    }
}
