package com.example.library_management_v2.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility-klass som hanterar skapande och validering av JWT-tokens.
 *
 * Denna klass fungerar som vårt "token-verktyg" - den kan:
 * 1. Skapa nya tokens när användare loggar in
 * 2. Läsa information från befintliga tokens
 * 3. Validera om tokens är giltiga och inte har gått ut
 */

@Component
public class JwtUtil {

    // Hemlig nyckel för att signera våra tokens - håll denna säker!
    // I produktion ska denna läsas från miljövariabel eller konfigfil
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token giltighets-tid: 24 timmar i millisekunder
    // Efter denna tid måste användaren logga in igen
    // private final long JWT_EXPIRATION = 1000 * 60 * 60 * 24; // 24 timmar

    // Token giltighets-tid: 15 minuter i millisekunder
    // Kort livslängd eftersom vi nu har refresh tokens för förnyelse
    private final long JWT_EXPIRATION = 1000 * 60 * 15; // 15 minuter


    public String extractUsername(String token) {
        // Subject (sub) i JWT innehåller normalt användarnamnet
        return extractClaim(token, Claims::getSubject);
    }


    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    public String extractRole(String token) {
        // Vi sparar rollen som en custom claim i token
        return extractClaim(token, claims -> claims.get("role", String.class));
    }


    // Generell metod för att extrahera specifik information från token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {
        try {
            // Parsa token med vår hemliga nyckel
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            // Om något går fel med token-parsing
            throw new RuntimeException("Ogiltigt JWT token", e);
        }
    }


    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Lägg till användarens roll i token
        // Vi tar första rollen (eftersom vårt system har en roll per användare)
        if (!userDetails.getAuthorities().isEmpty()) {
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            claims.put("role", role);
        }

        return createToken(claims, userDetails.getUsername());
    }


    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)                    // Extra information (t.ex. roller)
                .setSubject(subject)                  // Användarnamn
                .setIssuedAt(now)                     // När token skapades
                .setExpiration(expirationDate)        // När token går ut
                .signWith(SECRET_KEY)                 // Signera med vår hemliga nyckel
                .compact();                           // Konvertera till sträng
    }


    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);

            // Token är giltig om:
            // 1. Användarnamnet i token matchar den aktuella användaren
            // 2. Token inte har gått ut
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            // Om något går fel vid validering, betrakta token som ogiltigt
            return false;
        }
    }


    public Boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // JWT består av tre delar separerade med punkter: header.payload.signature
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
}