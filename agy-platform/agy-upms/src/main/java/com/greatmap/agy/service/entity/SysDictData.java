package com.greatmap.agy.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典数据实体（包含 parentId 字段以原生支持多级树形层级字典）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends IdEntity {

    private String dictType;
    private Long parentId; // 父节点 ID (0 表示根节点)
    private String dictLabel;
    private String dictValue;
    private Integer sortOrder;
    private String listClass;
    private String cssClass;
    private Integer status; // 1-正常 0-停用
    private String remark;
}
