package com.example.library_management_v2.exception;

// Skapar en ny exception för dubblett-författare

public class DuplicateAuthorException extends RuntimeException {

    public DuplicateAuthorException (String message) {
        super(message);
    }

}
