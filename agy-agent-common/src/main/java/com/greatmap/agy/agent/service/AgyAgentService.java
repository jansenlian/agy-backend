package com.greatmap.agy.agent.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AGY 智能体统一服务门面接口
 */
public interface AgyAgentService {

    /**
     * 同步智能对话
     *
     * @param memoryId    会话/用户唯一标识
     * @param userMessage 用户自然语言指令
     * @return 智能体最终决策答复
     */
    String chat(String memoryId, String userMessage);

    /**
     * 默认会话同步智能对话
     *
     * @param userMessage 用户自然语言指令
     * @return 智能体最终决策答复
     */
    default String chat(String userMessage) {
        return chat("default_session", userMessage);
    }

    /**
     * 响应式 SSE 流式打字机对话 (面向前端 WebSocket / EventSource 接入)
     *
     * @param memoryId    会话/用户唯一标识
     * @param userMessage 用户自然语言指令
     * @return SseEmitter 实例
     */
    SseEmitter chatStream(String memoryId, String userMessage);

    /**
     * 清空指定会话的历史记忆
     *
     * @param memoryId 会话/用户唯一标识
     */
    void clearMemory(String memoryId);
}
