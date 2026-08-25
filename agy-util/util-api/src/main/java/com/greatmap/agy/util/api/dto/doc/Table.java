package com.greatmap.agy.util.api.dto.doc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Swagger 接口详情表格模型
 *
 * @author AGY
 */
@Data
@Schema(description = "Swagger 接口表格模型")
public class Table implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "大标题 (Controller / Tag)")
    private String title;

    @Schema(description = "小标题")
    private String tag;

    @Schema(description = "请求 URL")
    private String url;

    @Schema(description = "接口描述")
    private String description;

    @Schema(description = "请求参数格式 (application/json, multipart/form-data 等)")
    private String requestForm;

    @Schema(description = "响应参数格式")
    private String responseForm;

    @Schema(description = "请求方式 (GET, POST, PUT, DELETE)")
    private String requestType;

    @Schema(description = "请求参数列表")
    private List<Request> requestList;

    @Schema(description = "响应参数列表")
    private List<Response> responseList;

    @Schema(description = "请求示例")
    private String requestParam;

    @Schema(description = "响应示例")
    private String responseParam;

    @Schema(description = "响应模型属性")
    private ModelAttr modelAttr = new ModelAttr();
}
