package com.greatmap.agy.business.api.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务演示 VO (业务系统专属)
 */
@Data
@Schema(description = "业务演示模块视图信息")
public class BusinessDemoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID (String 类型防 JS 精度丢失)", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "业务条目名称", example = "业务模块01")
    private String name;

    @Schema(description = "业务条目描述详情", example = "用于验证业务子系统独立运行能力的示范数据")
    private String description;

    @Schema(description = "创建时间", example = "2026-08-14 10:00:00")
    private LocalDateTime createTime;
}
