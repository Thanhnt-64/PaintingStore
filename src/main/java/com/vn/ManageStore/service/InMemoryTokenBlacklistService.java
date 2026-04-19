package com.vn.ManageStore.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * In-memory token blacklist service for testing when Redis is not available.
 */
@Service
@Profile("test")
public class InMemoryTokenBlacklistService {

    private final ConcurrentHashMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, Date expiry) {
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds((expiry.getTime() - System.currentTimeMillis()) / 1000);
        blacklistedTokens.put(token, expiryTime);
    }

    public boolean isBlacklisted(String token) {
        LocalDateTime expiryTime = blacklistedTokens.get(token);
        if (expiryTime == null) {
            return false;
        }
        
        if (expiryTime.isBefore(LocalDateTime.now())) {
            blacklistedTokens.remove(token); // cleanup expired
            return false;
        }
        
        return true;
    }

    // Clean up expired tokens
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}