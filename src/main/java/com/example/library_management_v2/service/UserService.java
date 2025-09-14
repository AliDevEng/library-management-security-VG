package com.example.library_management_v2.service;

import com.example.library_management_v2.dto.CreateUserDTO;
import com.example.library_management_v2.dto.UserDTO;
import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.exception.DuplicateUserException;
import com.example.library_management_v2.exception.UserNotFoundException;
import com.example.library_management_v2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityLoggingService securityLoggingService;


    public UserDTO getUserByEmail (String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Användare med email: " + email + " hittades inte!") );

        return convertToDTO(user);
    }


    /**
     * Konverterar en User entity till UserDTO (utan lösenord)
     * user User entity att konvertera
     * returnera Konverterad UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRegistrationDate(user.getRegistrationDate());
        // Lösenordet inkluderas inte i DTO:n av säkerhetsskäl
        return dto;
    }


    // Skapa en ny användare
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        try {
            // Kontroll om användaren redan finns
            if (userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
                // Logga dubblettregistreringsförsök
                securityLoggingService.logSecurityIncident("DUPLICATE_REGISTRATION",
                        "Attempt to register existing email: " + createUserDTO.getEmail());

                throw new DuplicateUserException
                        ("En användare med e-postadressen " + createUserDTO.getEmail() + " finns redan");
            }

            // Skapa ny användarentitet
            User user = new User();
            user.setFirstName(createUserDTO.getFirstName());
            user.setLastName(createUserDTO.getLastName());
            user.setEmail(createUserDTO.getEmail());
            user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
            user.setRegistrationDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            user.setRole("ROLE_USER");
            user.setEnabled(true);

            // Spara användaren
            User savedUser = userRepository.save(user);

            // SÄKERHETSLOGGNING: Logga lyckad registrering
            securityLoggingService.logUserRegistration(createUserDTO.getEmail());
            securityLoggingService.logDataChange("User", "CREATE", createUserDTO.getEmail());

            return convertToDTO(savedUser);

        } catch (DuplicateUserException e) {
            throw e;
        } catch (Exception e) {
            securityLoggingService.logSecurityIncident("REGISTRATION_ERROR",
                    "Unexpected error during registration for: " + createUserDTO.getEmail());
            throw new RuntimeException("Ett fel inträffade vid registrering", e);
        }
    }
}
