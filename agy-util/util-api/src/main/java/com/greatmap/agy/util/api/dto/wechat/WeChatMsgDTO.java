package com.greatmap.agy.util.api.dto.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 企业微信群机器人推送消息 DTO
 *
 * @author AGY
 */
@Data
@Schema(description = "企业微信群机器人消息请求参数")
public class WeChatMsgDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "自定义 Webhook 完整地址 (为空时使用系统默认配置)")
    private String webhook;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "推送文本或 Markdown 内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "消息类型：text / markdown (默认 markdown)")
    private String msgType = "markdown";

    @Schema(description = "是否 @所有人 (仅 text 类型生效)")
    private Boolean isAll = false;

    @Schema(description = "@手机号列表 (仅 text 类型生效)")
    private List<String> mobileList;
}
