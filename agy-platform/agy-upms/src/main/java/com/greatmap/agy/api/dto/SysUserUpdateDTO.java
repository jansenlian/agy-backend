package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户 DTO
 */
@Data
@Schema(description = "修改系统用户请求参数 (白名单防 Mass Assignment)")
public class SysUserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户主键 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户 ID 不能为空")
    private Long id;

    @Schema(description = "用户真实姓名", example = "张三")
    private String realName;

    @Schema(description = "手机号码", example = "13800138000")
    private String mobile;

    @Schema(description = "账号状态 (1-正常 0-禁用)", example = "1")
    private Integer status;
}
