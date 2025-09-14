package com.example.library_management_v2.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserDTO {

    private Long id;
    private String firstName;
    private String LastName;
    private String email;
    private String registrationDate;


    // En tom konstruktor som Spring kräver
    public UserDTO() {
    }

    // Setters och Getters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // public LocalDateTime getRegistrationDate() {
      //  return registrationDate;
    // }

    // public void setRegistrationDate(LocalDateTime registrationDate) {
       // this.registrationDate = registrationDate;
    // }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }
}
