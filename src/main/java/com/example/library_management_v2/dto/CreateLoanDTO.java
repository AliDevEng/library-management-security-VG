package com.example.library_management_v2.dto;

// En DTO för att ta emot indata när en användare vill låna en bok

import jakarta.validation.constraints.NotNull;

public class CreateLoanDTO {

    @NotNull(message = "Användar-ID måste anges")
    private Long userId;

    @NotNull(message = "Bok-ID måste anges")
    private Long bookId;

    // Tom konstruktor
    public CreateLoanDTO() {}

    // Getters och setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}