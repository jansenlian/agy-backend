package com.greatmap.agy.agent.service.impl;

import com.greatmap.agy.agent.core.UniversalAgent;
import com.greatmap.agy.agent.domain.dto.StreamChunkVO;
import com.greatmap.agy.agent.service.AgyAgentService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AGY 智能体统一服务实现类
 */
@Slf4j
@RequiredArgsConstructor
public class AgyAgentServiceImpl implements AgyAgentService {

    private final UniversalAgent universalAgent;
    private final ChatMemoryProvider chatMemoryProvider;

    @Override
    public String chat(String memoryId, String userMessage) {
        log.info("Agent 接收到同步交互请求 [memoryId: {}]: {}", memoryId, userMessage);
        return universalAgent.chat(memoryId, userMessage);
    }

    @Override
    public SseEmitter chatStream(String memoryId, String userMessage) {
        log.info("Agent 接收到流式交互请求 [memoryId: {}]: {}", memoryId, userMessage);
        SseEmitter emitter = new SseEmitter(180_000L); // 3 分钟长连接超时

        try {
            TokenStream tokenStream = universalAgent.chatStream(memoryId, userMessage);
            tokenStream
                    .onNext(token -> {
                        try {
                            emitter.send(StreamChunkVO.delta(token));
                        } catch (Exception e) {
                            log.warn("SSE 推流异常中断: {}", e.getMessage());
                        }
                    })
                    .onComplete(response -> {
                        try {
                            emitter.send(StreamChunkVO.finish());
                            emitter.complete();
                        } catch (Exception e) {
                            log.warn("SSE 正常关闭异常: {}", e.getMessage());
                        }
                    })
                    .onError(error -> {
                        log.error("Agent 流式推理异常: {}", error.getMessage(), error);
                        try {
                            emitter.send(StreamChunkVO.fail(error.getMessage()));
                            emitter.completeWithError(error);
                        } catch (Exception e) {
                            log.warn("SSE 异常关闭失败: {}", e.getMessage());
                        }
                    })
                    .start();
        } catch (Exception e) {
            log.error("启动 Agent 流式推理失败: {}", e.getMessage(), e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Override
    public void clearMemory(String memoryId) {
        log.info("清空会话记忆 [memoryId: {}]", memoryId);
        ChatMemory memory = chatMemoryProvider.get(memoryId);
        if (memory != null) {
            memory.clear();
        }
    }
}
