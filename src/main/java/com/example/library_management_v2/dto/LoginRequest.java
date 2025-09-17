package com.example.library_management_v2.dto;


// DTO för inloggningsförfrågningar
// Detta är vad klienten skickar när de vill logga in

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email får inte vara tom")
    @Email(message = "Ogiltig Email")
    private String email;

    @NotBlank(message = "Lösenord får inte vara tomt")
    private String password;

    // Tom konstruktör som krävs
    public LoginRequest () {}

    public LoginRequest (String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
