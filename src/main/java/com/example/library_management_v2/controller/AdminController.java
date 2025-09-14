package com.example.library_management_v2.controller;


import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.repository.BookRepository;
import com.example.library_management_v2.repository.LoanRepository;
import com.example.library_management_v2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping ("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;


    @GetMapping ("/dashboard")
    // Principal är ett Spring Security-objekt som automatiskt injiceras av Spring
    // när en användare är inloggad. Det innehåller information om den inloggade användaren.
    public Map<String, Object> adminDashboard(Principal principal){
        Map <String, Object> response = new HashMap<>();
        response.put("message", "Området är avsett endast för ADMIN");
        response.put("admin_user", principal.getName());

        // Systemstatistik som endast admins ska se
        response.put("total_users", userRepository.count());
        response.put("total_books", bookRepository.count());
        response.put("total_loans", loanRepository.count());

        // Visa information som är känslig och endast för administratörer
        response.put("security_note", "Denna data är ADMIN-ONLY Data");

        return response;
    }

    @GetMapping("/users")
    public Map<String, Object> getAllUsers(Principal principal) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Alla användare i systemet");
        response.put("requested_by", principal.getName());

        // Hämta alla användare från databasen
        List<User> users = userRepository.findAll();
        response.put("users", users);
        response.put("user_count", users.size());

        response.put("admin_note", "Endast administratörer kan se listan");

        return response;

    }

    @GetMapping("/system")
    public Map<String, Object> systemInfo(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Systemkonfiguration och säkerhetsinformation");
        response.put("admin_user", principal.getName());

        // Simulera känslig systeminformation
        response.put("active_sessions", "Simulerad data");
        response.put("security_events", "Simulerade säkerhetshändelser");
        response.put("system_health", "OK");

        response.put("warning", "Denna information är mycket känslig och eär ndast för systemadministratörer");

        return response;
    }

}
