package com.example.library_management_v2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cglib.SpringCglibInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {


    @GetMapping("/")
    public Map<String, Object> home(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Välkommen till Library Management System!");
        response.put("page", "home");

        // Kolla om användaren är inloggad
        if (principal != null) {
            response.put("user", principal.getName()); // Detta blir email:en
            response.put("authenticated", true);
            response.put("info", "Du är inloggad! Du kan komma åt /books nu.");
        } else {
            response.put("authenticated", false);
            response.put("info", "Du är inte inloggad. Denna sida kan alla se, men för att komma åt /books måste du logga in.");
        }

        return response;
    }

    /**
     * Alternativ URL för startsidan
     */
    @GetMapping("/home")
    public Map<String, Object> homeAlternative(Principal principal) {
        Map<String, Object> response = home(principal);
        response.put("page", "home-alternative");
        return response;
    }


    @GetMapping("/public/info")
    public Map<String, Object> publicInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Detta är publik information om systemet");
        response.put("version", "2.0");
        response.put("features", new String[]{"Bokhantering", "Användarhantering", "Lånesystem"});
        response.put("security_note", "Denna sida kräver ingen inloggning");
        return response;
    }


    // Endpoint för att förstå CSRF-skydd
    // Visar CSRF-token och förklarar säkerhetsmekanismer
    @GetMapping("/csrf-info")
    public Map<String, Object> csrfinfo(HttpServletRequest request, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Information om CSRF-skydd");

        // Hämta CSRF-token från request
        Object csrfToken = request.getAttribute("_csrf");
        if (csrfToken != null) {
            response.put("csrf_token_available", true);
            response.put("csrf_explanation", "CSRF-token krävs för alla POST/PUT/DELETE requests");
            response.put("security_note", "Detta skyddar mot Cross-Site Request Forgery attacker");


        } else {

            response.put("csrf_token_available", false);
            response.put("note", "CSRF-token kunde inte hämtas");

        }

        if (principal != null) {
            response.put("user", principal.getName());
            response.put("authenticated", true);
        } else {
            response.put("authenticated", false);
        }


        // Instruktioner för säker registrering
        response.put("registration_guide", Map.of(
                "step1", "För att registrera via API måste du inkludera CSRF-token",
                "step2", "Använd X-CSRF-TOKEN header i dina POST requests",
                "step3", "Eller använd webbformulär som Spring Security automatiskt skyddar"
        ));

        return response;
    }
}