package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户与角色关联关系 DTO (用于批量映射消除 N+1 查询)
 */
@Data
@Schema(description = "用户角色关联关系传输对象")
public class SysUserRoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    private Long userId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;
}
