package com.example.library_management_v2.dto;

import jakarta.validation.constraints.NotBlank;


public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token får inte vara tomt")
    private String refreshToken;

    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}