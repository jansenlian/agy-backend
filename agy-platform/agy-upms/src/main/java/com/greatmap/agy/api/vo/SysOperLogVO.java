package com.greatmap.agy.api.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志视图对象
 */
@Data
@Schema(description = "操作日志视图对象")
public class SysOperLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "操作日志 ID (String 防精度丢失)", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "模块标题", example = "用户管理")
    private String title;

    @Schema(description = "业务类型 (0-其它 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-强退 8-生成代码 9-清空数据)", example = "1")
    private Integer businessType;

    @Schema(description = "方法名称", example = "com.greatmap.agy.web.controller.SysUserController.createUser()")
    private String method;

    @Schema(description = "请求方式", example = "POST")
    private String requestMethod;

    @Schema(description = "操作人员账号", example = "admin")
    private String operName;

    @Schema(description = "请求 URL", example = "/api/users")
    private String operUrl;

    @Schema(description = "主机地址 / 操作 IP", example = "127.0.0.1")
    private String operIp;

    @Schema(description = "操作地点", example = "内网IP")
    private String operLocation;

    @Schema(description = "请求参数 (脱敏后)")
    private String operParam;

    @Schema(description = "返回参数 (截断后)")
    private String jsonResult;

    @Schema(description = "操作状态 (1-正常 0-异常)", example = "1")
    private Integer status;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "操作时间", example = "2026-08-14 10:00:00")
    private LocalDateTime operTime;

    @Schema(description = "消耗时间 (毫秒)", example = "45")
    private Long costTime;
}
