package com.greatmap.agy.business.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greatmap.agy.business.api.dto.BusinessDemoDTO;
import com.greatmap.agy.business.api.vo.BusinessDemoVO;
import com.greatmap.agy.business.service.entity.BusinessDemo;

/**
 * 业务演示 Service 接口 (业务微服务专属)
 */
public interface BusinessDemoService extends IService<BusinessDemo> {

    Page<BusinessDemoVO> getDemoPage(int pageNo, int pageSize, String name);

    Long createDemo(BusinessDemoDTO dto);
}
