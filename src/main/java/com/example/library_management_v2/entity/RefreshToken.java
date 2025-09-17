package com.example.library_management_v2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// RefreshToken Entity - för att hantera token-förnyelse
// Refresh tokens används för att få nya access tokens utan att logga in igen.
// De har längre livslängd än access tokens men sparas i databasen för säkerhet.

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    // Tom konstruktor
    public RefreshToken() {}

    // Konstruktor med parametrar
    public RefreshToken(String token, LocalDateTime expiryDate, User user) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }

    // Getters och setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Hjälpmetod för att kontrollera om token har gått ut
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }


}
