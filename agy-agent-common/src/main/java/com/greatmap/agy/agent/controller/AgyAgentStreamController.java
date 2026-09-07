package com.greatmap.agy.agent.controller;

import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.agent.domain.dto.AgentChatRequest;
import com.greatmap.agy.agent.service.AgyAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AGY 智能体通用 RESTful 与 SSE 流式控制器
 * 显式声明安全意图：需登录认证后方可使用智能体
 */
@PreAuthorize("isAuthenticated()")
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
     * GET 方式 SSE 打字机流式问答接口 (已废弃)
     *
     * @deprecated 浏览器原生 EventSource 规范无法自定义 Authorization 请求头，通过 URL 传递 ?token= 存在日志与历史记录泄露隐患。
     *             推荐统一迁移为 POST /api/agent/chat/stream 并配合前端 fetch + ReadableStream 消费。
     */
    @Deprecated(since = "1.0.0")
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
