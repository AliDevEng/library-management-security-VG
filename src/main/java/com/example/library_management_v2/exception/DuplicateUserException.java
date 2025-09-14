package com.example.library_management_v2.exception;


// Vi behöver hantera situationen när någon försöker skapa en användare
// med en e-postadress som redan finns i systemet
public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }
}