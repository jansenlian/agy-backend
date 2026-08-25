package com.greatmap.agy.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举定义
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    PARAM_VALIDATION_ERROR(400, "请求参数校验失败"),
    AUTH_CREDENTIALS_ERROR(400, "账号或密码错误"),
    USER_PASSWORD_ERROR(400, "账号或密码错误"), // 兼容旧引用
    USER_DISABLED(400, "账号已被禁用，请联系管理员"),
    REPEAT_SUBMIT(400, "请勿重复提交请求"),
    UNAUTHORIZED(401, "尚未登录或登录 Token 已过期"),
    FORBIDDEN(403, "暂无当前接口访问权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    INTERNAL_SERVER_ERROR(500, "系统内部繁忙，请稍后再试");

    private final int code;
    private final String message;
}
