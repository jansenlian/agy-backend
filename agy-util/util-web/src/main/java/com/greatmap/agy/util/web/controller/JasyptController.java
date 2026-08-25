package com.greatmap.agy.util.web.controller;

import com.greatmap.agy.common.annotation.Anonymous;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.common.utils.JasyptEncryptorUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Jasypt 配置文件安全加解密控制器
 * 统一委托 common 层的 JasyptEncryptorUtil 统一算法加密器，彻底消除算法漂移
 *
 * @author AGY
 */
@Slf4j
@Anonymous
@RestController
@RequestMapping("/jasypt")
@Tag(name = "Jasypt加解密工具", description = "用于 Spring Boot 配置文件 ENC(...) 敏感字段的加解密")
public class JasyptController {

    @Value("${jasypt.encryptor.password:AGY_FRAMEWORK_MASTER_SECRET_KEY_2026}")
    private String defaultPassword;

    @Operation(summary = "Jasypt 明文加密", description = "使用 Jasypt 对明文进行加密，返回加密字符串")
    @PostMapping("/encode")
    public Result<String> encode(
            @Parameter(description = "明文字符串") @RequestParam String text,
            @Parameter(description = "密钥/盐(为空则使用系统默认主密钥)") @RequestParam(required = false) String salt) {
        String pwd = StringUtils.defaultIfBlank(salt, defaultPassword);
        try {
            String ciphertext = JasyptEncryptorUtil.encrypt(text, pwd);
            return Result.success(ciphertext);
        } catch (Exception e) {
            log.error("Jasypt 加密异常: {}", e.getMessage(), e);
            return Result.fail("加密失败：" + e.getMessage());
        }
    }

    @Operation(summary = "Jasypt 密文解密", description = "使用 Jasypt 对密文进行解密，返回原始明文")
    @PostMapping("/decode")
    public Result<String> decode(
            @Parameter(description = "密文字符串 (支持带或不带 ENC(...))") @RequestParam String text,
            @Parameter(description = "密钥/盐(为空则使用系统默认主密钥)") @RequestParam(required = false) String salt) {
        String pwd = StringUtils.defaultIfBlank(salt, defaultPassword);
        try {
            String plaintext = JasyptEncryptorUtil.decrypt(text, pwd);
            return Result.success(plaintext);
        } catch (Exception e) {
            log.error("Jasypt 解密异常: {}", e.getMessage(), e);
            return Result.fail("解密失败：非有效 Jasypt 密文或密钥不匹配 (" + e.getMessage() + ")");
        }
    }
}
