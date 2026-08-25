package com.greatmap.agy.util.service.wechat.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatmap.agy.util.api.dto.wechat.WeChatMsgDTO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.util.service.wechat.service.WeChatNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业微信群机器人通知服务实现
 *
 * @author AGY
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatNotifyServiceImpl implements WeChatNotifyService {

    private final ObjectMapper objectMapper;

    @Value("${wechat.webhook.default:https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=7a9dffcb-4126-4371-8e57-b1e2f318f3e7}")
    private String defaultWebhook;

    @Override
    public String sendRobotMessage(WeChatMsgDTO reqDTO) {
        String webhook = StringUtils.isNotBlank(reqDTO.getWebhook()) ? reqDTO.getWebhook() : defaultWebhook;
        if (StringUtils.isBlank(webhook)) {
            throw new BusinessException("企业微信 Webhook 地址未配置");
        }

        Map<String, Object> payload = new HashMap<>();
        String msgType = StringUtils.defaultIfBlank(reqDTO.getMsgType(), "markdown");
        payload.put("msgtype", msgType);

        if ("markdown".equalsIgnoreCase(msgType)) {
            Map<String, Object> markdownMap = new HashMap<>();
            markdownMap.put("content", reqDTO.getContent());
            payload.put("markdown", markdownMap);
        } else {
            Map<String, Object> textMap = new HashMap<>();
            textMap.put("content", reqDTO.getContent());

            List<String> mentionedList = new ArrayList<>();
            if (Boolean.TRUE.equals(reqDTO.getIsAll())) {
                mentionedList.add("@all");
            }
            if (reqDTO.getMobileList() != null && !reqDTO.getMobileList().isEmpty()) {
                textMap.put("mentioned_mobile_list", reqDTO.getMobileList());
            }
            if (!mentionedList.isEmpty()) {
                textMap.put("mentioned_list", mentionedList);
            }
            payload.put("text", textMap);
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(payload);
            HttpResponse response = HttpRequest.post(webhook)
                    .body(jsonBody)
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                log.info("企微机器人消息推送成功：{}", response.body());
                return response.body();
            } else {
                log.error("企微机器人消息推送失败，HTTP 状态码 {}", response.getStatus());
                throw new BusinessException("企微机器人消息推送失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("企微机器人消息推送异常", e);
            throw new BusinessException("企微机器人消息推送异常：" + e.getMessage());
        }
    }
}
