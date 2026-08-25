package com.greatmap.agy.common.domain;

import com.greatmap.agy.common.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一三段式响应结构体
 *
 * @param <T> 数据泛型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码 (0 表示成功，非 0 表示各类业务错误)
     */
    private int code;

    /**
     * 响应提示消息
     */
    private String message;

    /**
     * 响应核心数据 Payload
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok() {
        return success(null);
    }

    public static <T> Result<T> ok(T data) {
        return success(data);
    }

    public static <T> Result<T> error(String message) {
        return error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), message);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> Result<T> fail(String message) {
        return error(message);
    }

    public static <T> Result<T> fail(int code, String message) {
        return error(code, message);
    }
}
