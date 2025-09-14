package com.example.library_management_v2.exception;

// Exception-klass för att hantera olika felscenarier

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }


}