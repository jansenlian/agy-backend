package com.greatmap.agy.util.service;

import com.greatmap.agy.util.api.dto.CryptoAsymmetricDTO;
import com.greatmap.agy.util.api.dto.CryptoHashDTO;
import com.greatmap.agy.util.api.dto.CryptoSymmetricDTO;
import com.greatmap.agy.util.api.vo.CryptoHashVO;
import com.greatmap.agy.util.api.vo.CryptoKeyPairVO;

/**
 * 加解密与散列计算业务接口
 */
public interface CryptoService {

    /**
     * 对称加密 (AES / SM4)
     */
    String encryptSymmetric(CryptoSymmetricDTO dto);

    /**
     * 对称解密 (AES / SM4)
     */
    String decryptSymmetric(CryptoSymmetricDTO dto);

    /**
     * 散列哈希计算 (MD5 / SHA1 / SHA256 / SHA512 / SM3)
     */
    CryptoHashVO calculateHash(CryptoHashDTO dto);

    /**
     * 生成非对称秘钥对 (SM2 / RSA)
     */
    CryptoKeyPairVO generateKeyPair(String algorithm);

    /**
     * 非对称加密 (公钥加密)
     */
    String encryptAsymmetric(CryptoAsymmetricDTO dto);

    /**
     * 非对称解密 (私钥解密)
     */
    String decryptAsymmetric(CryptoAsymmetricDTO dto);

    /**
     * 数字签名 (私钥签名)
     */
    String sign(CryptoAsymmetricDTO dto);

    /**
     * 验证数字签名 (公钥验签)
     */
    boolean verify(CryptoAsymmetricDTO dto);
}
