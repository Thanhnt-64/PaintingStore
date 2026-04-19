package com.vn.ManageStore.service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * In-memory OTP service for testing when Redis is not available.
 * To use this instead of Redis: Set profile to 'test' or remove Redis dependency temporarily.
 */
@Service
@Profile("test")
public class InMemoryOtpService {

    private final ConcurrentHashMap<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateLimitEntry> rateLimitStore = new ConcurrentHashMap<>();

    public String generateAndSaveOtp(String email) {
        String otp = RandomStringUtils.randomNumeric(6);
        OtpEntry entry = new OtpEntry(otp, LocalDateTime.now().plusMinutes(5));
        otpStore.put("otp:" + email, entry);
        
        // Update rate limit
        String rateKey = "rate:" + email;
        RateLimitEntry rateEntry = rateLimitStore.get(rateKey);
        if (rateEntry == null || rateEntry.resetTime.isBefore(LocalDateTime.now())) {
            rateLimitStore.put(rateKey, new RateLimitEntry(1, LocalDateTime.now().plusHours(1)));
        } else {
            rateEntry.count++;
        }
        
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String key = "otp:" + email;
        OtpEntry entry = otpStore.get(key);
        if (entry == null || entry.expiryTime.isBefore(LocalDateTime.now())) {
            otpStore.remove(key); // cleanup expired
            return false;
        }
        
        boolean match = entry.otp.equals(otp);
        if (match) {
            otpStore.remove(key); // consume OTP
        }
        return match;
    }

    public boolean canSend(String email, int maxPerHour) {
        String rateKey = "rate:" + email;
        RateLimitEntry entry = rateLimitStore.get(rateKey);
        
        if (entry == null || entry.resetTime.isBefore(LocalDateTime.now())) {
            return true; // No limit or expired limit
        }
        
        return entry.count < maxPerHour;
    }

    // Clean up expired entries periodically (simple cleanup)
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        otpStore.entrySet().removeIf(entry -> entry.getValue().expiryTime.isBefore(now));
        rateLimitStore.entrySet().removeIf(entry -> entry.getValue().resetTime.isBefore(now));
    }

    private static class OtpEntry {
        final String otp;
        final LocalDateTime expiryTime;
        
        OtpEntry(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    private static class RateLimitEntry {
        int count;
        final LocalDateTime resetTime;
        
        RateLimitEntry(int count, LocalDateTime resetTime) {
            this.count = count;
            this.resetTime = resetTime;
        }
    }
}