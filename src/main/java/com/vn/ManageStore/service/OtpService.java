package com.vn.ManageStore.service;

import java.time.Duration;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_RATE_PREFIX = "otp_rate:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String generateAndSaveOtp(String email) {
        String otp = RandomStringUtils.randomNumeric(6);
        String key = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(5));
        logger.debug("OTP saved to Redis with 5min TTL for key={}", key);
        logger.info("*** TEST OTP FOR EMAIL={}: {} (expires in 5 minutes) ***", email, otp);
        
        // increment rate counter
        String rateKey = OTP_RATE_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(rateKey);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(rateKey, Duration.ofHours(1));
            logger.debug("Rate limit key created with 1hour TTL for email={}", email);
        }
        logger.debug("OTP attempts for email={}: {}", email, attempts);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            logger.debug("OTP not found in Redis for key={}", key);
            return false;
        }
        boolean match = stored.equals(otp);
        if (match) {
            redisTemplate.delete(key);
            logger.debug("OTP verified and deleted from Redis for key={}", key);
        } else {
            logger.debug("OTP mismatch for email={}", email);
        }
        return match;
    }

    public boolean canSend(String email, int maxPerHour) {
        String rateKey = OTP_RATE_PREFIX + email;
        String v = redisTemplate.opsForValue().get(rateKey);
        if (v == null) {
            logger.debug("No rate limit record for email={}, can send", email);
            return true;
        }
        try {
            int count = Integer.parseInt(v);
            boolean canSend = count < maxPerHour;
            logger.debug("Rate limit check for email={}: attempts={}, maxPerHour={}, canSend={}", 
                         email, count, maxPerHour, canSend);
            return canSend;
        } catch (NumberFormatException ex) {
            logger.warn("Invalid rate limit value for email={}: {}", email, v);
            return true;
        }
    }
}

