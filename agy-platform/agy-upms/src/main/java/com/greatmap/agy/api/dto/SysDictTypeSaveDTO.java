package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型新增/修改 DTO
 */
@Data
@Schema(description = "数据字典类型新增与修改参数")
public class SysDictTypeSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典类型主键 ID (修改时必传)", example = "1")
    private Long id;

    @Schema(description = "字典名称", example = "用户性别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 1, max = 50, message = "字典名称长度必须为 1-50 个字符")
    private String dictName;

    @Schema(description = "字典类型唯一编码", example = "sys_user_sex", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型编码不能为空")
    @Size(min = 1, max = 50, message = "字典类型编码长度必须为 1-50 个字符")
    private String dictType;

    @Schema(description = "状态 (1-正常 0-停用)", example = "1", defaultValue = "1")
    private Integer status = 1;

    @Schema(description = "备注说明", example = "用户性别字典列表")
    @Size(max = 200, message = "备注信息不能超过 200 个字符")
    private String remark;
}
