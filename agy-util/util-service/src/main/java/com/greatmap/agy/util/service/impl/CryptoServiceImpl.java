package com.greatmap.agy.util.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SM4;
import com.greatmap.agy.util.api.dto.CryptoAsymmetricDTO;
import com.greatmap.agy.util.api.dto.CryptoHashDTO;
import com.greatmap.agy.util.api.dto.CryptoSymmetricDTO;
import com.greatmap.agy.util.api.vo.CryptoHashVO;
import com.greatmap.agy.util.api.vo.CryptoKeyPairVO;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.util.service.CryptoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class CryptoServiceImpl implements CryptoService {

    @Override
    public String encryptSymmetric(CryptoSymmetricDTO dto) {
        String algo = dto.getAlgorithm().toUpperCase();
        byte[] keyBytes = parseKeyBytes(dto.getKey(), 16);
        byte[] ivBytes = StrUtil.isNotBlank(dto.getIv()) ? parseKeyBytes(dto.getIv(), 16) : null;
        boolean isHex = "HEX".equalsIgnoreCase(dto.getFormat());

        try {
            if (algo.startsWith("SM4")) {
                SM4 sm4 = buildSm4(algo, keyBytes, ivBytes);
                return isHex ? sm4.encryptHex(dto.getText()) : sm4.encryptBase64(dto.getText());
            } else if (algo.startsWith("AES")) {
                AES aes = buildAes(algo, keyBytes, ivBytes);
                return isHex ? aes.encryptHex(dto.getText()) : aes.encryptBase64(dto.getText());
            } else {
                throw new BusinessException("不支持的对称加密算法: " + algo);
            }
        } catch (Exception e) {
            log.error("对称加密失败: algo={}, error={}", algo, e.getMessage());
            throw new BusinessException("对称加密失败: " + e.getMessage());
        }
    }

    @Override
    public String decryptSymmetric(CryptoSymmetricDTO dto) {
        String algo = dto.getAlgorithm().toUpperCase();
        byte[] keyBytes = parseKeyBytes(dto.getKey(), 16);
        byte[] ivBytes = StrUtil.isNotBlank(dto.getIv()) ? parseKeyBytes(dto.getIv(), 16) : null;

        try {
            if (algo.startsWith("SM4")) {
                SM4 sm4 = buildSm4(algo, keyBytes, ivBytes);
                return sm4.decryptStr(dto.getText(), StandardCharsets.UTF_8);
            } else if (algo.startsWith("AES")) {
                AES aes = buildAes(algo, keyBytes, ivBytes);
                return aes.decryptStr(dto.getText(), StandardCharsets.UTF_8);
            } else {
                throw new BusinessException("不支持的对称解密算法: " + algo);
            }
        } catch (Exception e) {
            log.error("对称解密失败: algo={}, error={}", algo, e.getMessage());
            throw new BusinessException("对称解密失败, 请核对密文或密钥是否正确");
        }
    }

    @Override
    public CryptoHashVO calculateHash(CryptoHashDTO dto) {
        String raw = dto.getText();
        if (StrUtil.isNotBlank(dto.getSalt())) {
            raw = raw + dto.getSalt();
        }

        return CryptoHashVO.builder()
                .md5(DigestUtil.md5Hex(raw))
                .sha1(DigestUtil.sha1Hex(raw))
                .sha256(DigestUtil.sha256Hex(raw))
                .sha512(DigestUtil.sha512Hex(raw))
                .sm3(SmUtil.sm3(raw))
                .build();
    }

    @Override
    public CryptoKeyPairVO generateKeyPair(String algorithm) {
        String algo = StrUtil.blankToDefault(algorithm, "SM2").toUpperCase();
        if ("SM2".equals(algo)) {
            SM2 sm2 = SmUtil.sm2();
            return CryptoKeyPairVO.builder()
                    .algorithm("SM2")
                    .publicKey(sm2.getPublicKeyBase64())
                    .privateKey(sm2.getPrivateKeyBase64())
                    .build();
        } else if ("RSA".equals(algo)) {
            RSA rsa = new RSA();
            return CryptoKeyPairVO.builder()
                    .algorithm("RSA")
                    .publicKey(rsa.getPublicKeyBase64())
                    .privateKey(rsa.getPrivateKeyBase64())
                    .build();
        } else {
            throw new BusinessException("仅支持生成 SM2 或 RSA 密钥对");
        }
    }

    @Override
    public String encryptAsymmetric(CryptoAsymmetricDTO dto) {
        String algo = dto.getAlgorithm().toUpperCase();
        if (StrUtil.isBlank(dto.getPublicKey())) {
            throw new BusinessException("非对称加密必须提供公钥 (publicKey)");
        }
        try {
            if ("SM2".equals(algo)) {
                SM2 sm2 = new SM2(null, dto.getPublicKey());
                return sm2.encryptBcd(dto.getText(), KeyType.PublicKey);
            } else if ("RSA".equals(algo)) {
                RSA rsa = new RSA(null, dto.getPublicKey());
                return rsa.encryptBase64(dto.getText(), KeyType.PublicKey);
            } else {
                throw new BusinessException("不支持的非对称算法: " + algo);
            }
        } catch (Exception e) {
            log.error("非对称加密失败: algo={}, error={}", algo, e.getMessage());
            throw new BusinessException("非对称加密失败: " + e.getMessage());
        }
    }

    @Override
    public String decryptAsymmetric(CryptoAsymmetricDTO dto) {
        String algo = dto.getAlgorithm().toUpperCase();
        if (StrUtil.isBlank(dto.getPrivateKey())) {
            throw new BusinessException("非对称解密必须提供私钥 (privateKey)");
        }
        try {
            if ("SM2".equals(algo)) {
                SM2 sm2 = new SM2(dto.getPrivateKey(), null);
                return sm2.decryptStrFromBcd(dto.getText(), KeyType.PrivateKey);
            } else if ("RSA".equals(algo)) {
                RSA rsa = new RSA(dto.getPrivateKey(), null);
                return rsa.decryptStr(dto.getText(), KeyType.PrivateKey);
            } else {
                throw new BusinessException("不支持的非对称算法: " + algo);
            }
        } catch (Exception e) {
            log.error("非对称解密失败: algo={}, error={}", algo, e.getMessage());
            throw new BusinessException("非对称解密失败, 请核对私钥或密文格式");
        }
    }

    @Override
    public String sign(CryptoAsymmetricDTO dto) {
        String algo = dto.getAlgorithm().toUpperCase();
        if (StrUtil.isBlank(dto.getPrivateKey())) {
            throw new BusinessException("数字签名必须提供私钥 (privateKey)");
        }
        try {
            if ("SM2".equals(algo)) {
                SM2 sm2 = new SM2(dto.getPrivateKey(), null);
                return HexUtil.encodeHexStr(sm2.sign(dto.getText().getBytes(StandardCharsets.UTF_8)));
            } else if ("RSA".equals(algo)) {
                Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, dto.getPrivateKey(), null);
                return Base64.encode(sign.sign(dto.getText().getBytes(StandardCharsets.UTF_8)));
            } else {
                throw new BusinessException("不支持的签名算法: " + algo);
            }
        } catch (Exception e) {
            log.error("签名失败: algo={}, error={}", algo, e.getMessage());
            throw new BusinessException("签名计算失败: " + e.getMessage());
        }
    }

    @Override
    public boolean verify(CryptoAsymmetricDTO dto) {
        String algo = dto.getAlgorithm().toUpperCase();
        if (StrUtil.isBlank(dto.getPublicKey()) || StrUtil.isBlank(dto.getSignature())) {
            throw new BusinessException("验签必须提供公钥与签名值");
        }
        try {
            if ("SM2".equals(algo)) {
                SM2 sm2 = new SM2(null, dto.getPublicKey());
                byte[] signBytes = HexUtil.decodeHex(dto.getSignature());
                return sm2.verify(dto.getText().getBytes(StandardCharsets.UTF_8), signBytes);
            } else if ("RSA".equals(algo)) {
                Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, null, dto.getPublicKey());
                byte[] signBytes = Base64.decode(dto.getSignature());
                return sign.verify(dto.getText().getBytes(StandardCharsets.UTF_8), signBytes);
            } else {
                throw new BusinessException("不支持的验签算法: " + algo);
            }
        } catch (Exception e) {
            log.warn("验签发生异常: error={}", e.getMessage());
            return false;
        }
    }

    private SM4 buildSm4(String algo, byte[] keyBytes, byte[] ivBytes) {
        if (algo.contains("CBC")) {
            if (ivBytes == null) {
                ivBytes = keyBytes;
            }
            return new SM4(Mode.CBC, Padding.PKCS5Padding, keyBytes, ivBytes);
        } else {
            return new SM4(Mode.ECB, Padding.PKCS5Padding, keyBytes);
        }
    }

    private AES buildAes(String algo, byte[] keyBytes, byte[] ivBytes) {
        if (algo.contains("CBC")) {
            if (ivBytes == null) {
                ivBytes = keyBytes;
            }
            return new AES(Mode.CBC, Padding.PKCS5Padding, keyBytes, ivBytes);
        } else if (algo.contains("CTR")) {
            if (ivBytes == null) {
                ivBytes = keyBytes;
            }
            return new AES(Mode.CTR, Padding.NoPadding, keyBytes, ivBytes);
        } else {
            return new AES(Mode.ECB, Padding.PKCS5Padding, keyBytes);
        }
    }

    private byte[] parseKeyBytes(String key, int defaultLen) {
        if (StrUtil.isBlank(key)) {
            throw new BusinessException("密钥不能为空");
        }
        if (key.matches("^[0-9a-fA-F]+$") && key.length() >= defaultLen * 2) {
            return HexUtil.decodeHex(key);
        }
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        if (bytes.length == defaultLen || bytes.length == 24 || bytes.length == 32) {
            return bytes;
        }
        byte[] result = new byte[defaultLen];
        System.arraycopy(bytes, 0, result, 0, Math.min(bytes.length, defaultLen));
        return result;
    }
}
