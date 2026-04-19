package com.vn.ManageStore.dto;

public class JwtResponse {
    public String token;
    public String tokenType = "Bearer";

    public JwtResponse(String token) {
        this.token = token;
    }
}
