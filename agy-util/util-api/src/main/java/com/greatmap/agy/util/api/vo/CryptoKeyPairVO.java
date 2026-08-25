package com.greatmap.agy.util.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 非对称秘钥对生成结果 VO (SM2 / RSA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "非对称秘钥对")
public class CryptoKeyPairVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "算法名称 (SM2, RSA)")
    private String algorithm;

    @Schema(description = "公钥 (Base64 或 Hex)")
    private String publicKey;

    @Schema(description = "私钥 (Base64 或 Hex)")
    private String privateKey;
}
