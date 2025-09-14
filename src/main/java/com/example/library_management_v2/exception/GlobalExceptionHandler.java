// src/main/java/com/example/librarymangementv2/exception/GlobalExceptionHandler.java
package com.example.library_management_v2.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Global exception handler för hela applikationen
@ControllerAdvice
public class GlobalExceptionHandler {


     // Hanterar valideringsfel för @Valid annoterade parametrar
     // ResponseEntity med valideringsfel och statuskod 400 (Bad Request)

    /*
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    */

    // Logger för säkerhetshändelser
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final Logger applicationLogger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // Skapar ett säkert felmeddelande med spårnings-ID
    private Map<String, Object> createSecureErrorResponse(String userMessage, String logMessage, HttpStatus status) {
        String errorId = UUID.randomUUID().toString().substring(0, 8);

        // Logga detaljerad information för utvecklare/admin
        applicationLogger.error("Error ID {}: {}", errorId, logMessage);

        // Returnera endast säker information till användaren
        Map<String, Object> response = new HashMap<>();
        response.put("error", userMessage);
        response.put("errorId", errorId);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());

        return response;
    }


     // Hanterar valideringsfel för @Valid annoterade parametrar

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Vi skapar en tom Map för att samla fel
        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {

            // fieldName = vilket fält som hade fel (t.ex. "password")
            String fieldName = ((FieldError) error).getField();

            // errorMessage = vad som var fel (t.ex. "Lösenordet måste vara minst 8 tecken")
            String errorMessage = error.getDefaultMessage();

            // put() = lägg detta fel i vår Map
            validationErrors.put(fieldName, errorMessage);
        });

        // Logga valideringsfel för säkerhetsanalys
        securityLogger.warn("Validation failed for fields: {}", validationErrors.keySet());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Valideringsfel i indata");
        response.put("validationErrors", validationErrors);
        response.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }



    /**
     * Hantera AuthorNotFoundException
     * ex Exception som kastas när en författare hittas inte
     * Vi får en ResponseEntity som innehåller felmeddelande (Not Found) 404
     */

    @ExceptionHandler(AuthorNotFoundException.class)
    public ResponseEntity<Object> handleAuthorNotFoundException(AuthorNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Hanterar IllegalArgumentException
     * ex Exception som kastas för ogiltiga argument/input
     * Vi får i return en ResponseEntity - Bad Request 400
     */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Hanterar generella RuntimeExceptions
     * ex Exception som kastas under Running
     * Vi får i return en ResponseEntity - Internal Server Error 500
     */

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Hanterar alla övriga Exceptions
     * @param ex Exception som kastats
     * @return ResponseEntity med felmeddelande och statuskod 500 (Internal Server Error)
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ett oväntat fel inträffade: " + ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicateAuthorException.class)
    public ResponseEntity<Object> handleDuplicateAuthorException(DuplicateAuthorException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Hanterar UserNotFoundException
     * ex Exception som kastats när en användare inte hittas
     * Returnera en ResponseEntity Not Found 404
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Hanterar DuplicateUserException
     * ex Exception som kastats när en användare med samma e-post redan finns
     * Returnera en ResponseEntity med felmeddelande Conflict 409
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Object> handleDuplicateUserException(DuplicateUserException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    // Statuskod 404 Not Found
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Object> handleBookNotFoundException(BookNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    // Statuskod 400 Bad Request
    @ExceptionHandler(BookNotAvailableException.class)
    public ResponseEntity<Object> handleBookNotAvailableException(BookNotAvailableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    // Statuskod 400 Not Found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    // Statuskod 400 Bad Request
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}