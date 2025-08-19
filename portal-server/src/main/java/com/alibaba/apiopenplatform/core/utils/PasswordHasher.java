package com.alibaba.apiopenplatform.core.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author zxd
 */
public class PasswordHasher {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String hash(String plainPassword) {
        return ENCODER.encode(plainPassword);
    }

    public static boolean verify(String plainPassword, String hashed) {
        return ENCODER.matches(plainPassword, hashed);
    }
} 