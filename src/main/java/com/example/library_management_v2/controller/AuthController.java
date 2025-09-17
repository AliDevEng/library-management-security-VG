package com.example.library_management_v2.controller;


import com.example.library_management_v2.config.JwtUtil;
import com.example.library_management_v2.dto.*;
import com.example.library_management_v2.entity.RefreshToken;
import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.exception.DuplicateUserException;
import com.example.library_management_v2.repository.UserRepository;
import com.example.library_management_v2.service.RefreshTokenService;
import com.example.library_management_v2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller - hanterar JWT-baserad inloggning och registrering
 *
 * Detta är vår "token-utgivare" som:
 * 1. Tar emot inloggningsförfrågningar
 * 2. Validerar användaruppgifter
 * 3. Genererar och returnerar JWT-tokens
 * 4. Hanterar användarregistrering
 */

@RestController
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;


    // Inloggnings-endpoint som returnerar JWT access token + refresh token
    // POST /auth/login
    @PostMapping("/login")

    /**
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {


        try {
            // Steg 1: Försök authentisera användaren med Spring Security
            // Detta kommer att kolla lösenordet mot vår databas
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Steg 2: Om authenitering lyckas hämta användardetaljer
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Hämta fullständig användarinformation från databasen
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow( () -> new RuntimeException("Användare hittades inte"));

            // Generera JWT-token
            String jwt = jwtUtil.generateToken(userDetails);



            // Steg 4: Skapa och returnera svar med token och användarinformation
            JwtResponse jwtResponse = new JwtResponse(
                    jwt,
                    user.getEmail(),
                    user.getRole(),
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName()
            );


            return ResponseEntity.ok(jwtResponse);

        } catch (BadCredentialsException e) {
            // Fel användarnamn och lösenord
            Map<String, String> error = new HashMap<>();
            error.put ("error", "Felaktig användarnamn eller lösenord");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (Exception e) {
            // Andra sort av fel
            Map<String, String> error = new HashMap<>();
            error.put("error", "Inloggning misslyckades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);

        }

    }
    */

    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Steg 1: Försök autentisera användaren med Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Steg 2: Om autentisering lyckas, hämta användardetaljer
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Hämta fullständig användarinformation från databasen
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Användare hittades inte"));

            // Steg 3: Generera JWT access token (kort livslängd)
            String accessToken = jwtUtil.generateToken(userDetails);

            // Steg 4: Skapa refresh token (lång livslängd)
            RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user.getId());

            // Steg 5: Skapa och returnera svar med båda tokens
            JwtResponse jwtResponse = new JwtResponse(
                    accessToken,
                    refreshTokenEntity.getToken(),
                    user.getEmail(),
                    user.getRole(),
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName()
            );

            return ResponseEntity.ok(jwtResponse);

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Felaktigt användarnamn eller lösenord");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Inloggning misslyckades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String requestRefreshToken = request.getRefreshToken();

            // Steg 1: Hitta och validera refresh token
            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                    .orElseThrow(() -> new RuntimeException("Ogiltigt refresh token"));

            // Steg 2: Kontrollera att token inte har gått ut
            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            // Steg 3: Hämta användaren från refresh token
            User user = refreshToken.getUser();

            // Steg 4: Skapa UserDetails för JWT-generering
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(user.getRole())
                    .build();

            // Steg 5: Generera nytt access token
            String newAccessToken = jwtUtil.generateToken(userDetails);

            // Steg 6: Optionellt - förnya även refresh token för extra säkerhet
            RefreshToken newRefreshToken = refreshTokenService.renewRefreshToken(refreshToken);

            // Steg 7: Returnera nya tokens
            JwtResponse jwtResponse = new JwtResponse(
                    newAccessToken,
                    newRefreshToken.getToken(),
                    user.getEmail(),
                    user.getRole(),
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName()
            );

            return ResponseEntity.ok(jwtResponse);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token refresh misslyckades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }





    // Registrerings-endpoint för nya användare
    // POST /auth/register
    /**
    @PostMapping("/register")
    public ResponseEntity<?> register (@Valid @RequestBody CreateUserDTO createUserDTO) {

        try {
            // Kontrollera om användaren redan finns
            if (userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
                throw new DuplicateUserException("En användare med denna e-postadress finns redan");
            }


            // Skapa ny användare
            User user = new User();
            user.setFirstName(createUserDTO.getFirstName());
            user.setLastName(createUserDTO.getLastName());
            user.setEmail(createUserDTO.getEmail());

            // Kryptera lösenordet innan vi sparar det (VIKTIGT!)
            user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));

            // Sätt standardvärden
            // user.setRegistrationDate(LocalDate.now());
            user.setRegistrationDate(LocalDate.now().toString());

            user.setRole("USER");  // Nya användare får USER-roll som standard
            user.setEnabled(true);

            // Spara användaren
            User savedUser = userRepository.save(user);

            // Skapa framgångsrikt svar
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Användare skapad framgångsrikt");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DuplicateUserException e) {

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (Exception e) {

            Map<String, String> error = new HashMap<>();
            error.put("error", "Registrering misslyckades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);

        }
    }
    */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        try {
            // Kontrollera om användaren redan finns
            if (userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
                throw new DuplicateUserException("En användare med denna e-postadress finns redan");
            }

            // Skapa ny användare
            User user = new User();
            user.setFirstName(createUserDTO.getFirstName());
            user.setLastName(createUserDTO.getLastName());
            user.setEmail(createUserDTO.getEmail());

            // Kryptera lösenordet innan vi sparar det (VIKTIGT!)
            user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));

            // Sätt standardvärden
            // Använd String-format för registrationDate (på grund av SQLite-kompatibilitet)
            user.setRegistrationDate(LocalDate.now().toString());
            user.setRole("USER");  // Nya användare får USER-roll som standard
            user.setEnabled(true);

            // Spara användaren
            User savedUser = userRepository.save(user);

            // Skapa framgångsrikt svar
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Användare skapad framgångsrikt");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DuplicateUserException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registrering misslyckades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }





    // Endpoint för att testa om JWT-token fungerar
    // Kräver giltig JWT-token i Authorization header
    // GET /auth/me

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("Användare hittades inte"));

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Du är inloggad som " + user.getEmail());
                response.put("userId", user.getId());
                response.put("email", user.getEmail());
                response.put("role", user.getRole());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Inte autentiserad");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Fel vid hämtning av användarinformation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



    // Endpoint för att logga ut (med JWT behöver klienten bara ta bort token)
    // POST /auth/logout
    /**
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        // Med JWT finns inget att "logga ut" på server-sidan
        // Klienten behöver bara ta bort token från sitt lokala storage
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utloggning lyckades. Ta bort token från klient-sidan.");
        return ResponseEntity.ok(response);
    }
    */

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String, String> request,
                                    Authentication authentication) {
        try {
            // Om vi har refresh token, ta bort det från databasen
            if (request != null && request.containsKey("refreshToken")) {
                String refreshToken = request.get("refreshToken");
                refreshTokenService.findByToken(refreshToken)
                        .ifPresent(token -> refreshTokenService.deleteByUserId(token.getUser().getId()));
            }
            // Alternativt, om användaren är autentiserad, ta bort alla deras refresh tokens
            else if (authentication != null && authentication.isAuthenticated()) {
                User user = userRepository.findByEmail(authentication.getName())
                        .orElse(null);
                if (user != null) {
                    refreshTokenService.deleteByUserId(user.getId());
                }
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Utloggning lyckades. Access token och refresh token har invaliderats.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utloggning genomförd (med varningar): " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
