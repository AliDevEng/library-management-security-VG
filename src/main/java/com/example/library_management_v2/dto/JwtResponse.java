package com.example.library_management_v2.dto;


public class JwtResponse {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";  // Token-typ (standard för JWT)
    private String email;
    private String role;
    private Long userId;
    private String firstName;
    private String lastName;

    // Tom konstruktor
    public JwtResponse() {}

    // Huvudkonstruktor med refresh token
    public JwtResponse(String accessToken, String refreshToken, String email, String role, Long userId, String firstName, String lastName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Bakåtkompatibel konstruktor (utan refresh token)
    public JwtResponse(String accessToken, String email, String role, Long userId, String firstName, String lastName) {
        this.accessToken = accessToken;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters och setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Bakåtkompatibilitet
    public String getToken() {
        return accessToken;
    }

    public void setToken(String token) {
        this.accessToken = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}