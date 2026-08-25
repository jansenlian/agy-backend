package com.greatmap.agy.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysLoginLogVO;
import com.greatmap.agy.service.entity.SysLoginLog;

public interface SysLoginLogService extends IService<SysLoginLog> {
    PageResultVO<SysLoginLogVO> getLogPage(int pageNo, int pageSize, String username, String ipAddress, Integer status);
    void recordLoginLog(String username, String ip, String browser, String os, Integer status, String msg);
}
