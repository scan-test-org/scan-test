package com.alibaba.apiopenplatform.core.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用Token黑名单服务，支持多用户类型Token的主动失效管理
 * 后续可无缝切换为Redis实现
 *
 * @author zxd
 */
@Service
public class TokenBlacklistService {
    // key: token，value: 过期时间戳（毫秒）
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * 将token加入黑名单
     * @param token token字符串
     * @param expireAt 过期时间戳（毫秒）
     */
    public void add(String token, long expireAt) {
        blacklist.put(token, expireAt);
    }

    /**
     * 检查token是否在黑名单
     * @param token token字符串
     * @return true=已失效，false=可用
     */
    public boolean isBlacklisted(String token) {
        Long expireAt = blacklist.get(token);
        if (expireAt == null) return false;
        if (System.currentTimeMillis() > expireAt) {
            blacklist.remove(token); // 自动清理过期
            return false;
        }
        return true;
    }
} 