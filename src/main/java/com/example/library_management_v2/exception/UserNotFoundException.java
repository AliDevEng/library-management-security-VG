package com.example.library_management_v2.exception;

// En exception för när användare inte hittas
public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException (String message) {
        super (message);
    }

}
