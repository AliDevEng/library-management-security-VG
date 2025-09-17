package com.example.library_management_v2.service;

import com.example.library_management_v2.entity.RefreshToken;
import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.repository.RefreshTokenRepository;
import com.example.library_management_v2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


// Denna service hanterar:
// - Skapande av nya refresh tokens
// - Validering av befintliga refresh tokens
// - Borttagning av utgångna tokens

@Service
@Transactional
public class RefreshTokenService {

    // Refresh token livslängd: 7 dagar
    private final int REFRESH_TOKEN_DURATION_DAYS = 7;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;


    // Skapa en ny refresh token för användaren
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Användare hittades inte med ID: " + userId));

        // Ta bort eventuell befintlig refresh token för denna användare
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        // Skapa ny refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString()); // Generera unikt token
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_DURATION_DAYS));

        return refreshTokenRepository.save(refreshToken);
    }


    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }


    // Verifiera att en refresh token är giltig
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token har gått ut. Vänligen logga in igen.");
        }
        return token;
    }


    // Ta bort refresh token för en användare (vid utloggning)
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Användare hittades inte med ID: " + userId));

        refreshTokenRepository.deleteByUser(user);
    }


    // Rensa alla utgångna refresh tokens från databasen
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }


    // Förnya refresh token (skapa ny med samma användare)
    public RefreshToken renewRefreshToken(RefreshToken oldToken) {
        // Verifiera att det gamla token inte är utgånget
        verifyExpiration(oldToken);

        // Ta bort gamla token
        refreshTokenRepository.delete(oldToken);

        // Skapa nytt token
        return createRefreshToken(oldToken.getUser().getId());
    }
}