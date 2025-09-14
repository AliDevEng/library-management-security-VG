package com.example.library_management_v2.service;

import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataInitService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initData() {
        // Skapa testanvändare om de inte finns
        if (userRepository.findByEmail("user@test.com").isEmpty()) {
            User user = new User();
            user.setEmail("user@test.com");
            user.setPassword(passwordEncoder.encode("password123"));  // Kryptera lösenord
            user.setRole("USER");
            user.setEnabled(true);
            userRepository.save(user);
        }

        if (userRepository.findByEmail("admin@test.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@test.com");
            // passwordEncoder krypterar lösenordet och sparar den
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);
        }

        System.out.println("✅ Testanvändare skapade:");
        System.out.println("   👤 user@test.com / password123 (USER)");
        System.out.println("   👑 admin@test.com / admin123 (ADMIN)");
    }
}
