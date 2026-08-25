package com.greatmap.agy.util.service.wechat.service;

import com.greatmap.agy.util.api.dto.wechat.WeChatMsgDTO;

/**
 * 企业微信群机器人通知服务接口
 *
 * @author AGY
 */
public interface WeChatNotifyService {

    /**
     * 发送企业微信群机器人消息 (支持 Markdown / Text / @所有人)
     *
     * @param reqDTO 消息体
     * @return 发送结果说明
     */
    String sendRobotMessage(WeChatMsgDTO reqDTO);
}
