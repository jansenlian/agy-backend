package com.greatmap.agy.business.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.agy.business.api.dto.BusinessDemoDTO;
import com.greatmap.agy.business.api.vo.BusinessDemoVO;
import com.greatmap.agy.business.service.entity.BusinessDemo;
import com.greatmap.agy.business.service.mapper.BusinessDemoMapper;
import com.greatmap.agy.business.service.service.BusinessDemoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务演示 Service 实现类 (业务微服务专属)
 */
@Service
public class BusinessDemoServiceImpl extends ServiceImpl<BusinessDemoMapper, BusinessDemo> implements BusinessDemoService {

    @Override
    public Page<BusinessDemoVO> getDemoPage(int pageNo, int pageSize, String name) {
        Page<BusinessDemo> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<BusinessDemo> wrapper = new LambdaQueryWrapper<BusinessDemo>()
                .like(StrUtil.isNotBlank(name), BusinessDemo::getName, name)
                .orderByDesc(BusinessDemo::getCreateTime);

        Page<BusinessDemo> resultPage = this.page(page, wrapper);
        Page<BusinessDemoVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<BusinessDemoVO> voList = resultPage.getRecords().stream()
                .map(e -> BeanUtil.copyProperties(e, BusinessDemoVO.class))
                .toList();
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Long createDemo(BusinessDemoDTO dto) {
        BusinessDemo entity = BeanUtil.copyProperties(dto, BusinessDemo.class);
        this.save(entity);
        return entity.getId();
    }
}
