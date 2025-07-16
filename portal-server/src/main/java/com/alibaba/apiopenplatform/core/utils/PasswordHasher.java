package com.alibaba.apiopenplatform.core.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密与校验工具类，基于Spring Security的BCryptPasswordEncoder
 * 推荐用于管理员和开发者账号体系的密码存储
 *
 * @author zxd
 */
public class PasswordHasher {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 生成密码哈希
     * @param plainPassword 明文密码
     * @return 哈希字符串
     */
    public static String hash(String plainPassword) {
        return ENCODER.encode(plainPassword);
    }

    /**
     * 校验密码
     * @param plainPassword 明文密码
     * @param hashed 哈希值
     * @return 是否匹配
     */
    public static boolean verify(String plainPassword, String hashed) {
        return ENCODER.matches(plainPassword, hashed);
    }
} 