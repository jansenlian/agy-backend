package com.greatmap.agy.util.web.controller;

import com.greatmap.agy.util.api.dto.wechat.WeChatMsgDTO;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.util.service.wechat.service.WeChatNotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业微信群机器人消息推送控制器
 *
 * @author AGY
 */
@RestController
@RequestMapping("/wechat")
@Tag(name = "企业微信通知", description = "企业微信群机器人消息与告警推送接口")
@RequiredArgsConstructor
public class WeChatNotifyController {

    private final WeChatNotifyService weChatNotifyService;

    @Operation(summary = "推送企业微信群机器人消息 (Markdown / Text)")
    @PostMapping("/send")
    public Result<String> sendMessage(@Valid @RequestBody WeChatMsgDTO reqDTO) {
        String resp = weChatNotifyService.sendRobotMessage(reqDTO);
        return Result.success(resp);
    }
}
