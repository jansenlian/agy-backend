package com.greatmap.agy.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 带有统一主键 ID 的实体基类
 * 适用于 95% 包含单列主键的业务表 (自动继承 BaseEntity 的 5 大审计与逻辑删除字段)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class IdEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 统一主键 ID (默认雪花算法 19位大整数)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
}
