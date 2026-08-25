package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增用户 DTO
 */
@Data
@Schema(description = "创建系统用户请求参数")
public class SysUserCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "登录用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "初始密码密文 (RSA加密)", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @lombok.ToString.Exclude
    private String password;

    @Schema(description = "用户真实姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "手机号码", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;
}
