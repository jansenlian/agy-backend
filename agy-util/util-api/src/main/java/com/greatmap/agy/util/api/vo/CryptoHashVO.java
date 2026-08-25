package com.greatmap.agy.util.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 散列哈希计算结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "散列哈希计算结果")
public class CryptoHashVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "MD5 32位小写摘要")
    private String md5;

    @Schema(description = "SHA-1 40位小写摘要")
    private String sha1;

    @Schema(description = "SHA-256 64位小写摘要")
    private String sha256;

    @Schema(description = "SHA-512 128位小写摘要")
    private String sha512;

    @Schema(description = "国密 SM3 64位小写摘要")
    private String sm3;
}
