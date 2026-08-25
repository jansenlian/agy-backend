package com.greatmap.agy.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysOperLogVO;
import com.greatmap.agy.service.entity.SysOperLog;

public interface SysOperLogService extends IService<SysOperLog> {
    PageResultVO<SysOperLogVO> getOperLogPage(int pageNo, int pageSize, String title, String operName, Integer businessType, Integer status);
}
