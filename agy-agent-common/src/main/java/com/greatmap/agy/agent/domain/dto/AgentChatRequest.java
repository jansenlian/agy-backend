package com.greatmap.agy.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 智能体对话请求入参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话隔离唯一标识 (如 userId, sessionId, 订单号等，默认 default_session)
     */
    private String memoryId;

    /**
     * 用户发起的自然语言指令或提问
     */
    @NotBlank(message = "用户指令或提问内容不能为空")
    private String message;

    /**
     * 临时自定义系统提示词 (可选，不填则使用系统默认全局配置)
     */
    private String systemPrompt;
}
