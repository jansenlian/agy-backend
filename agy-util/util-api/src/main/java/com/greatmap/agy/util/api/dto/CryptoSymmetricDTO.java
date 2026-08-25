package com.greatmap.agy.util.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 对称加解密请求 DTO (AES / SM4)
 */
@Data
@Schema(description = "对称加解密请求参数")
public class CryptoSymmetricDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "加密算法不能为空")
    @Schema(description = "算法名称 (AES-GCM, AES-CBC, AES-ECB, SM4-CBC, SM4-ECB)", example = "SM4-CBC")
    private String algorithm;

    @NotBlank(message = "密钥不能为空")
    @Schema(description = "加解密密钥 (16/32 位字符串或 16进制 Hex)", example = "1234567890123456")
    private String key;

    @Schema(description = "初始化向量 IV (CBC/GCM 模式可选)", example = "1234567890123456")
    private String iv;

    @NotBlank(message = "待处理内容不能为空")
    @Schema(description = "待加密的明文或待解密的密文", example = "Hello GreatMap AGY 2026")
    private String text;

    @Schema(description = "输出/输入格式 (BASE64 或 HEX)", example = "BASE64")
    private String format = "BASE64";
}
