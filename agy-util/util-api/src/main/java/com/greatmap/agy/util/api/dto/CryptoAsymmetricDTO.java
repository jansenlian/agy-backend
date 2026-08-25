package com.greatmap.agy.util.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 非对称加解密与签名验签请求 DTO (SM2 / RSA)
 */
@Data
@Schema(description = "非对称加解密与签名参数")
public class CryptoAsymmetricDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "算法名称不能为空")
    @Schema(description = "算法名称 (SM2, RSA)", example = "SM2")
    private String algorithm;

    @Schema(description = "公钥 (加密或验签时必填)")
    private String publicKey;

    @Schema(description = "私钥 (解密或签名时必填)")
    private String privateKey;

    @NotBlank(message = "待处理内容不能为空")
    @Schema(description = "明文、密文或签名原始内容")
    private String text;

    @Schema(description = "签名值 (验签时必填)")
    private String signature;
}
