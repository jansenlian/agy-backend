package com.greatmap.agy.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysLoginLogVO;
import com.greatmap.agy.service.entity.SysLoginLog;
import com.greatmap.agy.service.mapper.SysLoginLogMapper;
import com.greatmap.agy.service.service.SysLoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public PageResultVO<SysLoginLogVO> getLogPage(int pageNo, int pageSize, String username, String ipAddress, Integer status) {
        Page<SysLoginLog> page = new Page<>(pageNo, pageSize);
        
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
                .like(StrUtil.isNotBlank(username), SysLoginLog::getUsername, username)
                .like(StrUtil.isNotBlank(ipAddress), SysLoginLog::getIpAddress, ipAddress)
                .eq(status != null, SysLoginLog::getStatus, status)
                .orderByDesc(SysLoginLog::getLoginTime);

        Page<SysLoginLog> logPage = this.page(page, wrapper);

        List<SysLoginLogVO> voList = logPage.getRecords().stream()
                .map(l -> BeanUtil.copyProperties(l, SysLoginLogVO.class))
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, logPage.getTotal(), logPage.getSize(), logPage.getCurrent(), logPage.getPages());
    }

    @Override
    @Async("taskExecutor")
    public void recordLoginLog(String username, String ip, String browser, String os, Integer status, String msg) {
        String location = "未知位置";
        try {
            location = com.greatmap.agy.common.utils.IpUtils.getLocation(ip);
        } catch (Exception e) {
            log.warn("异步解析 IP [{}] 地理位置异常: {}", ip, e.getMessage());
        }

        SysLoginLog sysLoginLog = new SysLoginLog();
        sysLoginLog.setUsername(username);
        sysLoginLog.setIpAddress(ip);
        sysLoginLog.setLocation(location);
        sysLoginLog.setBrowser(browser);
        sysLoginLog.setOs(os);
        sysLoginLog.setStatus(status);
        sysLoginLog.setMsg(msg);
        sysLoginLog.setLoginTime(LocalDateTime.now());

        try {
            this.save(sysLoginLog);
        } catch (Exception e) {
            log.warn("保存登录日志失败: {}", e.getMessage());
        }
    }
}
