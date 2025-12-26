package com.example.demo.dto;

public class AuthResponseDto {
    private String token;
    private String expiresAt;

    public AuthResponseDto() {}
    
    public AuthResponseDto(String token, String expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}