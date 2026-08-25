package com.greatmap.agy.agent.controller;

import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.agent.domain.dto.AgentChatRequest;
import com.greatmap.agy.agent.service.AgyAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AGY 智能体通用 RESTful 与 SSE 流式控制器
 * 开箱即用，支持前端标准 Fetch / EventSource 直连
 */
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgyAgentStreamController {

    private final AgyAgentService agyAgentService;

    /**
     * 同步智能问答接口
     */
    @PostMapping("/chat")
    public Result<String> chat(@Validated @RequestBody AgentChatRequest request) {
        String memoryId = request.getMemoryId() != null && !request.getMemoryId().isBlank()
                ? request.getMemoryId()
                : "default_session";
        String answer = agyAgentService.chat(memoryId, request.getMessage());
        return Result.success(answer);
    }

    /**
     * GET 方式 SSE 打字机流式问答接口 (适合浏览器 EventSource 原生调用)
     */
    @GetMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chatStreamGet(
            @RequestParam(defaultValue = "default_session") String memoryId,
            @RequestParam String message) {
        return agyAgentService.chatStream(memoryId, message);
    }

    /**
     * POST 方式 SSE 打字机流式问答接口 (适合长文本 Prompt 提交)
     */
    @PostMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chatStreamPost(@Validated @RequestBody AgentChatRequest request) {
        String memoryId = request.getMemoryId() != null && !request.getMemoryId().isBlank()
                ? request.getMemoryId()
                : "default_session";
        return agyAgentService.chatStream(memoryId, request.getMessage());
    }

    /**
     * 清空指定会话记忆
     */
    @DeleteMapping("/memory/{memoryId}")
    public Result<Void> clearMemory(@PathVariable String memoryId) {
        agyAgentService.clearMemory(memoryId);
        return Result.success();
    }
}
