package com.greatmap.agy.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greatmap.agy.common.dict.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志视图 VO
 */
@Data
@Schema(description = "系统登录审计日志视图信息")
public class SysLoginLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志主键 ID", example = "1")
    private Long id;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "登录客户端 IP 地址", example = "127.0.0.1")
    private String ipAddress;

    @Schema(description = "登录地理物理位置", example = "内网IP")
    private String location;

    @Schema(description = "浏览器名称与主版本", example = "Chrome 127")
    private String browser;

    @Schema(description = "操作系统名称", example = "Mac OS X")
    private String os;

    @Schema(description = "登录状态 (1-成功 0-失败)", example = "1")
    @Dict(dictCode = "sys_login_status")
    private Integer status; // 1-成功 0-失败

    @Schema(description = "操作提示信息 (如：登录成功/密码错误)", example = "登录成功")
    private String msg;

    @Schema(description = "登录请求发生时间", example = "2026-08-14 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
