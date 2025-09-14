// src/main/java/com/example/library_management_v2/controller/UserController.java
package com.example.library_management_v2.controller;

import com.example.library_management_v2.dto.CreateUserDTO;
import com.example.library_management_v2.dto.UserDTO;
import com.example.library_management_v2.exception.UserNotFoundException;
import com.example.library_management_v2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Hämta användare via email
     * email E-postadressen att söka efter
     * Returnera användaren som UserDTO
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        try {
            UserDTO user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            // Låt GlobalExceptionHandler hantera detta
            throw e;
        }
    }

    // Skapa en ny användare
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        return userService.createUser(createUserDTO);
    }


}