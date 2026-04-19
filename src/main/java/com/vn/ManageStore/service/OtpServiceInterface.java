package com.vn.ManageStore.service;

public interface OtpServiceInterface {
    String generateAndSaveOtp(String email);
    boolean verifyOtp(String email, String otp);
    boolean canSend(String email, int maxPerHour);
}