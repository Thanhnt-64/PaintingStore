package com.vn.ManageStore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.ManageStore.dto.ApiResponse;
import com.vn.ManageStore.dto.JwtResponse;
import com.vn.ManageStore.dto.LoginRequest;
import com.vn.ManageStore.dto.OtpRequest;
import com.vn.ManageStore.dto.RegisterRequest;
import com.vn.ManageStore.dto.ResetCompleteRequest;
import com.vn.ManageStore.dto.ResetRequest;
import com.vn.ManageStore.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) throws Exception {
        logger.info(">>> CONTROLLER: POST /api/auth/register called with email={}", req.email);
        authService.register(req.username, req.email, req.password);
        logger.info("<<< CONTROLLER: POST /api/auth/register completed");
        return ResponseEntity.ok(new ApiResponse("Registration initiated. Check email for OTP."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody OtpRequest req) {
        boolean ok = authService.verifyOtpForEmail(req.email, req.otp);
        if (!ok) return ResponseEntity.badRequest().body(new ApiResponse("Invalid OTP"));
        // enable user
        java.util.Optional<com.vn.ManageStore.domain.User> userOpt = authService.getUserService().findByEmail(req.email);
        if (userOpt.isPresent()) {
            com.vn.ManageStore.domain.User u = userOpt.get();
            u.setEnabled(true);
            authService.getUserService().save(u);
            return ResponseEntity.ok(new ApiResponse("Account activated"));
        }
        return ResponseEntity.badRequest().body(new ApiResponse("User not found"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String token = authService.login(req.usernameOrEmail, req.password);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(new ApiResponse("Logged out"));
    }

    @PostMapping("/reset-request")
    public ResponseEntity<?> resetRequest(@RequestBody ResetRequest req) {
        authService.requestPasswordReset(req.email);
        return ResponseEntity.ok(new ApiResponse("If the email exists, an OTP has been sent"));
    }

    @PostMapping("/reset-verify")
    public ResponseEntity<?> resetVerify(@RequestBody OtpRequest req) {
        boolean ok = authService.verifyOtpForEmail(req.email, req.otp);
        if (!ok) return ResponseEntity.badRequest().body(new ApiResponse("Invalid OTP"));
        return ResponseEntity.ok(new ApiResponse("OTP valid"));
    }

    @PostMapping("/reset-complete")
    public ResponseEntity<?> resetComplete(@RequestBody ResetCompleteRequest req) {
        authService.completeReset(req.email, req.otp, req.newPassword);
        return ResponseEntity.ok(new ApiResponse("Password updated"));
    }
}
