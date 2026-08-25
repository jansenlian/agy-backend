package com.greatmap.agy.util.api.dto.doc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Swagger 请求参数模型
 *
 * @author AGY
 */
@Data
@Schema(description = "Swagger 接口请求参数")
public class Request implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "参数名")
    private String name;

    @Schema(description = "数据类型")
    private String type;

    @Schema(description = "参数类型 (query, path, body, header 等)")
    private String paramType;

    @Schema(description = "是否必填")
    private Boolean require;

    @Schema(description = "说明描述")
    private String remark;

    @Schema(description = "复杂对象属性")
    private ModelAttr modelAttr;
}
