package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 重置密码 DTO
 */
@Data
@Schema(description = "重置用户密码请求参数 (密码需经前端 RSA 加密)")
public class SysUserResetPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "重置后的新密码密文 (RSA加密)", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密文密码不能为空")
    @lombok.ToString.Exclude
    private String password;
}
