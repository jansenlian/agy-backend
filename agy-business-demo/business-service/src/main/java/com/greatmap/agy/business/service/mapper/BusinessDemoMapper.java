package com.greatmap.agy.business.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.business.service.entity.BusinessDemo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 业务演示 Mapper 接口 (业务微服务专属)
 */
@Mapper
public interface BusinessDemoMapper extends BaseMapper<BusinessDemo> {
}
