package com.alibaba.apiopenplatform.support.common;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zh
 */
@Slf4j
public class Encryptor {

    private static String ROOT_KEY;

    private static AES getAes() {
        if (StrUtil.isBlank(ROOT_KEY)) {
            ROOT_KEY = SpringUtil.getProperty("encryption.root-key");
        }

        if (StrUtil.isBlank(ROOT_KEY)) {
            throw new RuntimeException("Encryption root key is not set");
        }

        return SecureUtil.aes(ROOT_KEY.getBytes(CharsetUtil.CHARSET_UTF_8));
    }

    public static String encrypt(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        try {
            return getAes().encryptHex(value);
        } catch (Exception e) {
            log.error("Encrypt failed: {}", e.getMessage());
            return value;
        }
    }

    public static String decrypt(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        try {
            return getAes().decryptStr(value);
        } catch (Exception e) {
            log.error("Decrypt failed: {}", e.getMessage());
            return value;
        }
    }
}
