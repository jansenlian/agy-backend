package com.greatmap.agy.business.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务演示实体类 (业务微服务专属)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_demo")
public class BusinessDemo extends IdEntity {

    private String name;

    private String description;
}
