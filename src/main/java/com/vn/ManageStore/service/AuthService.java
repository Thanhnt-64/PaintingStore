package com.vn.ManageStore.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vn.ManageStore.domain.User;
import com.vn.ManageStore.security.JwtUtil;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final int OTP_MAX_PER_HOUR = 5;

    public void register(String username, String email, String password) throws Exception {
        logger.info("Registration requested for email={}", email);
        if (userService.findByEmail(email).isPresent()) {
            logger.warn("Registration failed - email already in use: {}", email);
            throw new IllegalArgumentException("Email already in use");
        }
        userService.createUser(username, email, password);
        logger.debug("User created for email={}, username={}", email, username);
        
        // send OTP
        if (!otpService.canSend(email, OTP_MAX_PER_HOUR)) {
            logger.warn("OTP rate limit exceeded for email={}", email);
            throw new IllegalStateException("Too many OTP requests, please try later");
        }
        String otp = otpService.generateAndSaveOtp(email);
        logger.debug("OTP generated for email={} (value not logged for security)", email);
        
        // send email (do not log OTP)
        String html = "<p>Your activation code: <strong>" + otp + "</strong></p>";
        try {
            emailService.sendOtpEmail(email, "Account activation code", html);
            logger.info("Activation email sent to {}", email);
        } catch (Exception ex) {
            logger.warn("Failed to send activation email to {} (OTP saved in Redis, check logs): {}", email, ex.getMessage());
            // DO NOT throw - registration still succeeds, OTP is in Redis for manual testing
            logger.info("OTP saved to Redis for manual testing. Check logs above for OTP value");
        }
    }

    public String login(String usernameOrEmail, String password) {
        logger.info("Login attempt for usernameOrEmail={}", usernameOrEmail);
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
            );
            UserDetails ud = (UserDetails) auth.getPrincipal();
            List<String> roles = ud.getAuthorities().stream().map(a -> a.getAuthority()).toList();
            String token = jwtUtil.generateToken(ud.getUsername(), roles);
            logger.info("Login successful for user={}, roles={}", ud.getUsername(), roles);
            return token;
        } catch (AuthenticationException ex) {
            logger.info("Login failed for usernameOrEmail={}: {}", usernameOrEmail, ex.getMessage());
            throw ex;
        }
    }

    public void logout(String token) {
        if (!jwtUtil.validate(token)) {
            logger.debug("Logout called with invalid token");
            return;
        }
        tokenBlacklistService.blacklistToken(token, jwtUtil.getExpirationDate(token));
        logger.info("Token blacklisted during logout");
    }

    public void requestPasswordReset(String email) {
        logger.info("Password reset requested for email={}", email);
        if (userService.findByEmail(email).isEmpty()) {
            logger.debug("Password reset: email not found (silently ignored): {}", email);
            return; // don't reveal
        }
        if (!otpService.canSend(email, OTP_MAX_PER_HOUR)) {
            logger.warn("OTP rate limit exceeded for password reset email={}", email);
            throw new IllegalStateException("Too many OTP requests, please try later");
        }
        String otp = otpService.generateAndSaveOtp(email);
        logger.debug("Password reset OTP generated for email={} (value not logged)", email);
        
        String html = "<p>Your password reset code: <strong>" + otp + "</strong></p>";
        try {
            emailService.sendOtpEmail(email, "Password reset code", html);
            logger.info("Password reset email sent to {}", email);
        } catch (Exception ex) {
            logger.error("Failed to send password reset email to {}: {}", email, ex.getMessage());
            throw new RuntimeException("Failed to send email");
        }
    }

    public boolean verifyOtpForEmail(String email, String otp) {
        boolean ok = otpService.verifyOtp(email, otp);
        logger.debug("OTP verification result for email={}: {}", email, ok ? "SUCCESS" : "FAILED");
        return ok;
    }

    public void completeReset(String email, String otp, String newPassword) {
        boolean ok = otpService.verifyOtp(email, otp);
        if (!ok) {
            logger.warn("Complete reset failed - invalid OTP for email={}", email);
            throw new IllegalArgumentException("Invalid OTP");
        }
        User u = userService.findByEmail(email).orElseThrow(() -> {
            logger.error("Complete reset - user not found for email={}", email);
            return new IllegalArgumentException("User not found");
        });
        u.setPassword(passwordEncoder.encode(newPassword));
        userService.save(u);
        logger.info("Password successfully updated for email={}", email);
    }

    public UserService getUserService() {
        return userService;
    }
}
