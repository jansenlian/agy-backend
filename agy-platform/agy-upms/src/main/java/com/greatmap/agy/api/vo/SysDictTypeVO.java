package com.greatmap.agy.api.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典类型视图对象
 */
@Data
@Schema(description = "数据字典类型视图对象")
public class SysDictTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典类型主键 ID (String 防精度丢失)", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "字典名称", example = "用户性别")
    private String dictName;

    @Schema(description = "字典类型编码", example = "sys_user_sex")
    private String dictType;

    @Schema(description = "状态 (1-正常 0-停用)", example = "1")
    private Integer status;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "创建时间", example = "2026-08-14 10:00:00")
    private LocalDateTime createTime;
}
