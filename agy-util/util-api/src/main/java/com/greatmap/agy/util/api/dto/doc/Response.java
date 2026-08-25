package com.greatmap.agy.util.api.dto.doc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Swagger 响应参数模型
 *
 * @author AGY
 */
@Data
@Schema(description = "Swagger 接口响应参数")
public class Response implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "返回参数说明")
    private String description;

    @Schema(description = "参数名")
    private String name;

    @Schema(description = "备注")
    private String remark;
}
