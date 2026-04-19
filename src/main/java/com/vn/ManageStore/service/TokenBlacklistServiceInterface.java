package com.vn.ManageStore.service;

public interface TokenBlacklistServiceInterface {
    void blacklistToken(String token, java.util.Date expiry);
    boolean isBlacklisted(String token);
}