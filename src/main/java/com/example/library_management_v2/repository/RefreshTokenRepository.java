package com.example.library_management_v2.repository;

import com.example.library_management_v2.entity.RefreshToken;
import com.example.library_management_v2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Hitta refresh token by token string
    Optional<RefreshToken> findByToken(String token);

    // Hitta refresh token by användare
    Optional<RefreshToken> findByUser(User user);

    // Radera refresh token by användare (vid utloggning)
    @Modifying
    @Transactional
    void deleteByUser(User user);

    // Radera alla utgångna tokens (för cleanup)
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);
}