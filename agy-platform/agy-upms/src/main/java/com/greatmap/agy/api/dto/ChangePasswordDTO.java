package com.greatmap.agy.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码 DTO
 */
@Data
@Schema(description = "个人修改登录密码请求参数 (原密码与新密码均需 RSA 加密)")
public class ChangePasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前原密码密文 (RSA加密)", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "原密码不能为空")
    @lombok.ToString.Exclude
    private String oldPassword;

    @Schema(description = "新密码密文 (RSA加密)", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "新密码长度不能少于 6 位")
    @lombok.ToString.Exclude
    private String newPassword;
}
