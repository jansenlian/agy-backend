package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据项新增/修改 DTO
 */
@Data
@Schema(description = "数据字典项新增与修改参数")
public class SysDictDataSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典数据项主键 ID (修改时必传)", example = "1")
    private Long id;

    @Schema(description = "所属字典类型编码", example = "sys_user_sex", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型编码不能为空")
    private String dictType;

    @Schema(description = "字典标签显示文本", example = "男", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典标签不能为空")
    @Size(min = 1, max = 50, message = "字典标签长度必须为 1-50 个字符")
    private String dictLabel;

    @Schema(description = "字典键值实际存储值", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典键值不能为空")
    @Size(min = 1, max = 50, message = "字典键值长度必须为 1-50 个字符")
    private String dictValue;

    @Schema(description = "字典显示排序号 (升序)", example = "1", defaultValue = "0")
    private Integer sortOrder = 0;

    @Schema(description = "字典数据状态 (1-正常 0-停用)", example = "1", defaultValue = "1")
    private Integer status = 1;

    @Schema(description = "备注信息说明", example = "男性性别标识")
    @Size(max = 200, message = "备注信息不能超过 200 个字符")
    private String remark;
}
