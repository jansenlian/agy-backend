package com.greatmap.agy.common.event;

import com.greatmap.agy.common.domain.SysOperLogDTO;
import org.springframework.context.ApplicationEvent;

/**
 * 操作日志 Spring Event 异步事件
 */
public class SysOperLogEvent extends ApplicationEvent {

    public SysOperLogEvent(SysOperLogDTO logDTO) {
        super(logDTO);
    }

    public SysOperLogDTO getLogDTO() {
        return (SysOperLogDTO) getSource();
    }
}
