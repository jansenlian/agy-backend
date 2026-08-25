package com.greatmap.agy.util.web.controller;

import com.greatmap.agy.util.api.dto.CryptoAsymmetricDTO;
import com.greatmap.agy.util.api.dto.CryptoHashDTO;
import com.greatmap.agy.util.api.dto.CryptoSymmetricDTO;
import com.greatmap.agy.util.api.vo.CryptoHashVO;
import com.greatmap.agy.util.api.vo.CryptoKeyPairVO;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.util.service.CryptoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "国密与通用加解密工具接口", description = "提供 SM2/SM3/SM4 及 AES/RSA/MD5 等加解密与摘要计算")
@RestController
@RequestMapping("/api/v1/crypto")
@RequiredArgsConstructor
public class CryptoController {

    private final CryptoService cryptoService;

    @Operation(summary = "对称加密 (AES / SM4)")
    @PostMapping("/symmetric/encrypt")
    public Result<String> encryptSymmetric(@Valid @RequestBody CryptoSymmetricDTO dto) {
        return Result.success(cryptoService.encryptSymmetric(dto));
    }

    @Operation(summary = "对称解密 (AES / SM4)")
    @PostMapping("/symmetric/decrypt")
    public Result<String> decryptSymmetric(@Valid @RequestBody CryptoSymmetricDTO dto) {
        return Result.success(cryptoService.decryptSymmetric(dto));
    }

    @Operation(summary = "哈希与摘要计算 (MD5 / SHA / SM3)")
    @PostMapping("/hash")
    public Result<CryptoHashVO> calculateHash(@Valid @RequestBody CryptoHashDTO dto) {
        return Result.success(cryptoService.calculateHash(dto));
    }

    @Operation(summary = "生成非对称秘钥对 (SM2 / RSA)")
    @GetMapping("/keypair")
    public Result<CryptoKeyPairVO> generateKeyPair(@RequestParam(defaultValue = "SM2") String algorithm) {
        return Result.success(cryptoService.generateKeyPair(algorithm));
    }

    @Operation(summary = "非对称加密 (公钥加密)")
    @PostMapping("/asymmetric/encrypt")
    public Result<String> encryptAsymmetric(@Valid @RequestBody CryptoAsymmetricDTO dto) {
        return Result.success(cryptoService.encryptAsymmetric(dto));
    }

    @Operation(summary = "非对称解密 (私钥解密)")
    @PostMapping("/asymmetric/decrypt")
    public Result<String> decryptAsymmetric(@Valid @RequestBody CryptoAsymmetricDTO dto) {
        return Result.success(cryptoService.decryptAsymmetric(dto));
    }

    @Operation(summary = "数字签名 (私钥签名)")
    @PostMapping("/asymmetric/sign")
    public Result<String> sign(@Valid @RequestBody CryptoAsymmetricDTO dto) {
        return Result.success(cryptoService.sign(dto));
    }

    @Operation(summary = "验证数字签名 (公钥验签)")
    @PostMapping("/asymmetric/verify")
    public Result<Boolean> verify(@Valid @RequestBody CryptoAsymmetricDTO dto) {
        return Result.success(cryptoService.verify(dto));
    }
}
