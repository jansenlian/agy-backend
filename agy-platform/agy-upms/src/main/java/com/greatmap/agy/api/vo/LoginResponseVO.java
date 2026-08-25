package com.greatmap.agy.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录与令牌刷新响应 VO (支持双令牌无感刷新)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录与令牌刷新认证响应结果 (双令牌模式)")
public class LoginResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 32位随机 UUID 访问令牌 (2小时有效期)
     */
    @Schema(description = "访问令牌 (请求头 Authorization: Bearer {token})", example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6")
    private String token;

    /**
     * 访问令牌 (别名，与 token 完全一致)
     */
    @Schema(description = "访问令牌 (别名)", example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6")
    private String accessToken;

    /**
     * 32位随机 UUID 刷新令牌 (7天有效期，用于无感换取新 Access Token)
     */
    @Schema(description = "刷新令牌 (7天有效期，用于 401 时静默换取新令牌)", example = "f6e5d4c3b2a10987654321fedcba0987")
    private String refreshToken;

    /**
     * 令牌类型 (Bearer)
     */
    @Schema(description = "令牌类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 访问令牌有效秒数 (默认 7200 秒)
     */
    @Schema(description = "访问令牌有效时长 (秒)", example = "7200")
    @Builder.Default
    private long expiresIn = 7200L;

    /**
     * 当前登录用户信息
     */
    @Schema(description = "当前登录用户基本信息")
    private SysUserVO user;
}
