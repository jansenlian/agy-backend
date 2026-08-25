package com.greatmap.agy.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends IdEntity {

    private String dictName;
    private String dictType;
    private Integer status; // 1-正常 0-停用
    private String remark;
}
