package com.greatmap.agy.business.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 业务演示 DTO (业务系统专属，不与平台 UPMS 冗余)
 */
@Data
@Schema(description = "业务演示模块创建参数")
public class BusinessDemoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "业务演示条目名称", example = "业务模块01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "业务名称不能为空")
    private String name;

    @Schema(description = "业务演示描述详情", example = "用于验证业务子系统独立运行能力的示范数据")
    private String description;
}
