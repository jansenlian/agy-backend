package com.greatmap.agy.agent.core;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * AGY 通用智能体声明式接口
 * 支持同步问答与 TokenStream 响应式流式输出
 */
public interface UniversalAgent {

    @SystemMessage("""
            你是一名由 AGY 智能架构驱动的企业级超级智能业务助理.
            你具备严谨的逻辑推理能力，并且有权调用业务系统注册的本地 Java 工具来查询数据与执行操作.
            请始终基于工具返回的真实数据进行回答，保持专业、严谨且亲切的语气.
            """)
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);

    @SystemMessage("""
            你是一名由 AGY 智能架构驱动的企业级超级智能业务助理.
            你具备严谨的逻辑推理能力，并且有权调用业务系统注册的本地 Java 工具来查询数据与执行操作.
            请始终基于工具返回的真实数据进行回答，保持专业、严谨且亲切的语气.
            """)
    TokenStream chatStream(@MemoryId String memoryId, @UserMessage String userMessage);
}
