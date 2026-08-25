package com.greatmap.agy.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志传输 DTO
 */
@Data
public class SysOperLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模块标题
     */
    private String title;

    /**
     * 业务类型 (1-新增 2-修改 3-删除 4-授权 5-导出 6-清空)
     */
    private Integer businessType;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 请求方式 (GET/POST/PUT/DELETE)
     */
    private String requestMethod;

    /**
     * 操作人员账号/姓名
     */
    private String operName;

    /**
     * 请求 URL
     */
    private String operUrl;

    /**
     * 主机 IP 地址
     */
    private String operIp;

    /**
     * 请求参数 JSON
     */
    private String operParam;

    /**
     * 响应结果 JSON
     */
    private String jsonResult;

    /**
     * 操作状态 (1-成功 0-失败)
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;
}
