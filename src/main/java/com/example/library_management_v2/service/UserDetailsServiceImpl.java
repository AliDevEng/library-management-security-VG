package com.example.library_management_v2.service;

import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Hämta användare från databas
        User user = userRepository.findByEmail(email)
                .orElseThrow( () -> new UsernameNotFoundException("User not found: " + email) );

        // Konvertera till Spring Security's UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())                  // Använd email som username
                .password(user.getPassword())               // Krypterat lösenord från DB
                .authorities(user.getRole())      // Spring Security kräver "ROLE_" prefix
                .disabled(!user.isEnabled())                // Invertera enabled för disabled
                .build();
    }
}


