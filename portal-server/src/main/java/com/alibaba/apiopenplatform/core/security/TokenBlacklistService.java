package com.alibaba.apiopenplatform.core.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zxd
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void add(String token, long expireAt) {
        blacklist.put(token, expireAt);
    }

    public boolean isBlacklisted(String token) {
        Long expireAt = blacklist.get(token);
        if (expireAt == null) return false;
        if (System.currentTimeMillis() > expireAt) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
} 