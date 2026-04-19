package com.vn.ManageStore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void blacklistToken(String token, Date expiry) {
        long ttlSeconds = (expiry.getTime() - System.currentTimeMillis()) / 1000;
        if (ttlSeconds <= 0) ttlSeconds = 60;
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String token) {
        String v = redisTemplate.opsForValue().get(BLACKLIST_PREFIX + token);
        return v != null;
    }
}
