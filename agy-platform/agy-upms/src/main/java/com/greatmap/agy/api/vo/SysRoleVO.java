package com.greatmap.agy.api.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色视图 VO
 */
@Data
@Schema(description = "系统角色视图信息")
public class SysRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色主键 ID (String 类型防 JS 精度丢失)", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "角色唯一编码", example = "ROLE_ADMIN")
    private String roleCode;

    @Schema(description = "角色名称", example = "超级管理员")
    private String roleName;

    @Schema(description = "角色状态 (1-正常 0-禁用)", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2026-08-14 10:00:00")
    private LocalDateTime createTime;
}
