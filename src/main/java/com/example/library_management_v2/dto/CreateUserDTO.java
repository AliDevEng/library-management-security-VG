package com.example.library_management_v2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateUserDTO {

    @NotBlank(message = "Förnamn får inte vara tomt")
    private String firstName;

    @NotBlank(message = "Efternamn får inte vara tomt")
    private String lastName;

    @NotBlank(message = "E-postadress får inte vara tom")
    @Email(message = "Ogiltig e-postadress")
    private String email;

    /*
    @NotBlank(message = "Lösenord får inte vara tomt")
    @Size(min = 6, message = "Lösenordet måste vara minst 6 tecken långt")
    private String password;
    */

    // Nya lösenord policy för bättre lösenord
    @NotBlank(message = "Lösenord får inte vara tomt")
    @Size(min = 8, message = "Lösenordet måste innehålla minst 8 tecken")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
            message = "Lösenordet måste innehålla både bokstäver och siffror"
    )
    private String password;

    // Tom konstruktor
    public CreateUserDTO() {}

    // Getters och setters
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