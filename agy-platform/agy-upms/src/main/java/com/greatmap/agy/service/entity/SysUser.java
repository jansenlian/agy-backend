package com.greatmap.agy.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends IdEntity {

    private String username;
    private String password;
    private String realName;
    private String mobile;
    private Integer status;
}
