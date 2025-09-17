package com.example.library_management_v2.dto;


// DTO som skickas tillbaka när inloggning lyckas
// Innehåller JWT-token och användarinformation

public class JwtResponse {


    private String token;
    private String type = "Bearer";  // Token-typ (standard för JWT)
    private String email;
    private String role;
    private Long userId;
    private String firstName;
    private String lastName;

    // Tom konstruktör som krävs av SpringBoot
    public JwtResponse() {
    }

    public JwtResponse(String token, String email, String role, Long userId, String firstName, String lastName) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
