package com.greatmap.agy.dwg.model.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "success"),
    BAD_REQUEST(400, "请求参数不合法"),
    FILE_EMPTY(4001, "上传的文件为空"),
    UNSUPPORTED_FORMAT(4002, "不支持的文件格式,仅支持.dwg和.dxf"),
    DWG_CONVERT_FAILED(5001, "DWG转换为DXF失败,请检查文件是否损坏或环境工具链"),
    DXF_PARSE_FAILED(5002, "DXF文件解析异常"),
    SYSTEM_ERROR(5000, "系统内部繁忙,请稍后重试");

    private final int code;
    private final String message;
}
