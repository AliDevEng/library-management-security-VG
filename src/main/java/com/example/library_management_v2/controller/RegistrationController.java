package com.example.library_management_v2.controller;

import com.example.library_management_v2.dto.CreateUserDTO;
import com.example.library_management_v2.dto.UserDTO;
import com.example.library_management_v2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


 // Dedicated controller för användarregistrering med CSRF-säkerhet
 // Denna controller demonstrerar hur CSRF-skydd fungerar i praktiken

@RestController
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private UserService userService;


    @GetMapping
    public Map<String, Object> getRegistrationForm(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registreringsformulär");
        response.put("method", "POST till /register för att registrera användare");

        // Hämta CSRF-token för detta request
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            response.put("csrf_token", csrfToken.getToken());
            response.put("csrf_header_name", csrfToken.getHeaderName());
            response.put("csrf_parameter_name", csrfToken.getParameterName());

            // Instruktioner för hur man använder CSRF-token
            response.put("csrf_instructions", Map.of(
                    "postman_header", "Lägg till header: " + csrfToken.getHeaderName() + " = " + csrfToken.getToken(),
                    "form_field", "Lägg till hidden field: " + csrfToken.getParameterName() + " = " + csrfToken.getToken(),
                    "explanation", "CSRF-token måste skickas med alla POST/PUT/DELETE requests"
            ));
        }

        // Visa exempel på korrekt registreringsdata
        response.put("example_data", Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "john.doe@example.com",
                "password", "password123"
        ));

        response.put("validation_rules", Map.of(
                "password_min_length", "8 tecken",
                "password_complexity", "Måste innehålla både bokstäver och siffror",
                "email", "Måste vara giltig e-postadress",
                "duplicates", "E-postadress får inte redan finnas"
        ));

        return response;
    }

    /**
     * POST endpoint för säker användarregistrering
     * Denna metod kräver giltig CSRF-token för att fungera
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registerUser(@Valid @RequestBody CreateUserDTO createUserDTO,
                                            HttpServletRequest request) {

        // Registrera användaren via UserService
        UserDTO newUser = userService.createUser(createUserDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Användare skapad framgångsrikt!");
        response.put("user", newUser);
        response.put("security_note", "Registreringen lyckades eftersom korrekt CSRF-token skickades");

        // Visa att CSRF-skydd fungerade
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            response.put("csrf_verification", "CSRF-token validerades korrekt");
        }

        response.put("next_steps", Map.of(
                "login", "Du kan nu logga in med din e-postadress och lösenord",
                "access", "Efter inloggning kan du komma åt /books och andra skyddade resurser"
        ));

        return response;
    }

    /**
     * Endpoint för att demonstrera vad som händer utan CSRF-token
     * (Detta kommer att blockeras av Spring Security)
     */
    @GetMapping("/csrf-demo")
    public Map<String, Object> csrfDemo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CSRF-demonstration");
        response.put("explanation", "Detta är en GET-request som alltid fungerar");
        response.put("security_note", "Endast GET-requests tillåts utan CSRF-token");
        response.put("test_instruction", "Försök skicka POST till /register utan CSRF-token för att se vad som händer");

        return response;
    }
}