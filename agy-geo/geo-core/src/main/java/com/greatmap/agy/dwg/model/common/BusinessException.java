package com.greatmap.agy.dwg.model.common;

/**
 * 空间地理与 CAD 解析专属业务异常
 * 继承底层 agy-common 的通用 BusinessException
 */
public class BusinessException extends com.greatmap.agy.common.exception.BusinessException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage);
    }
}
