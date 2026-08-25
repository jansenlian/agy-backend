package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页查询 DTO
 */
@Data
@Schema(description = "通用基础分页查询参数")
public class PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码 (从1开始)", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private int pageNo = 1;

    @Schema(description = "每页记录条数", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 500, message = "每页条数不能超过 500")
    private int pageSize = 10;
}
