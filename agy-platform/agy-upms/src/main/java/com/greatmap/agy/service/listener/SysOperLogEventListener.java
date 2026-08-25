package com.greatmap.agy.service.listener;

import cn.hutool.core.bean.BeanUtil;
import com.greatmap.agy.common.domain.SysOperLogDTO;
import com.greatmap.agy.common.event.SysOperLogEvent;
import com.greatmap.agy.service.entity.SysOperLog;
import com.greatmap.agy.service.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听操作日志事件并异步写入 sys_oper_log 表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysOperLogEventListener {

    private final SysOperLogService sysOperLogService;

    @Async
    @EventListener
    public void handleOperLogEvent(SysOperLogEvent event) {
        try {
            SysOperLogDTO dto = event.getLogDTO();
            SysOperLog entity = BeanUtil.copyProperties(dto, SysOperLog.class);
            sysOperLogService.save(entity);
        } catch (Exception e) {
            log.error("保存操作日志异步事件处理失败: {}", e.getMessage(), e);
        }
    }
}
