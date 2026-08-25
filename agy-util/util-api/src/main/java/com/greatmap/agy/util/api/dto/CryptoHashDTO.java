package com.greatmap.agy.util.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 散列哈希计算请求 DTO
 */
@Data
@Schema(description = "散列哈希计算请求参数")
public class CryptoHashDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "待计算哈希的原始文本不能为空")
    @Schema(description = "待计算的原始文本", example = "agy-util-service-2026")
    private String text;

    @Schema(description = "可选加盐 (Salt)", example = "agy")
    private String salt;
}
