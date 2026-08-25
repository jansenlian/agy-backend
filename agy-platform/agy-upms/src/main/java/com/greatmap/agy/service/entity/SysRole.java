package com.greatmap.agy.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends IdEntity {

    private String roleCode;
    private String roleName;
    private Integer status;
}
