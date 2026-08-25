package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色新增/修改 DTO (避免 Entity 批量赋值 Mass Assignment 漏洞)
 */
@Data
@Schema(description = "角色新增与修改参数")
public class SysRoleSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色主键 ID (修改时必传)", example = "1")
    private Long id;

    @Schema(description = "角色名称", example = "运维管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须为 2-50 个字符")
    private String roleName;

    @Schema(description = "角色唯一编码", example = "ROLE_OPS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 50, message = "角色编码长度必须为 2-50 个字符")
    private String roleCode;

    @Schema(description = "角色描述说明", example = "负责系统运维与监控")
    @Size(max = 200, message = "描述信息不能超过 200 个字符")
    private String description;
}
