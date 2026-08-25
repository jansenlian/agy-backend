package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求 DTO
 */
@Data
@Schema(description = "用户登录请求参数 (密码需经前端 RSA 加密传输)")
public class LoginRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "登录用户名/账号", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "前端 RSA-OAEP 加密后的密码密文", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @lombok.ToString.Exclude
    private String password;
}
