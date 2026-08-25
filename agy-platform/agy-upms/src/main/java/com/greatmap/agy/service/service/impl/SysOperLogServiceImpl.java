package com.greatmap.agy.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.agy.api.vo.PageResultVO;
import com.greatmap.agy.api.vo.SysOperLogVO;
import com.greatmap.agy.service.entity.SysOperLog;
import com.greatmap.agy.service.mapper.SysOperLogMapper;
import com.greatmap.agy.service.service.SysOperLogService;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    public PageResultVO<SysOperLogVO> getOperLogPage(int pageNo, int pageSize, String title, String operName, Integer businessType, Integer status) {
        Page<SysOperLog> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<SysOperLog>()
                .like(StrUtil.isNotBlank(title), SysOperLog::getTitle, title)
                .like(StrUtil.isNotBlank(operName), SysOperLog::getOperName, operName)
                .eq(businessType != null, SysOperLog::getBusinessType, businessType)
                .eq(status != null, SysOperLog::getStatus, status)
                .orderByDesc(SysOperLog::getOperTime);

        Page<SysOperLog> resultPage = this.page(page, wrapper);
        java.util.List<com.greatmap.agy.api.vo.SysOperLogVO> voList = cn.hutool.core.bean.BeanUtil.copyToList(resultPage.getRecords(), com.greatmap.agy.api.vo.SysOperLogVO.class);
        return PageResultVO.of(voList, resultPage.getTotal(), resultPage.getSize(), resultPage.getCurrent());
    }
}
