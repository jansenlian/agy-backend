package com.greatmap.agy.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.greatmap.agy.common.entity.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_login_log")
public class SysLoginLog extends IdEntity {

    private String username;
    private String ipAddress;
    private String location;
    private String browser;
    private String os;
    private Integer status;
    private String msg;
    private LocalDateTime loginTime;
}
