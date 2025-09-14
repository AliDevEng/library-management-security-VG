package com.example.library_management_v2.controller;

//  Vi skapar en controller för att hantera lån-relaterade endpoints:
import com.example.library_management_v2.dto.CreateLoanDTO;
import com.example.library_management_v2.dto.LoanDTO;
import com.example.library_management_v2.exception.UserNotFoundException;
import com.example.library_management_v2.service.LoanService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
public class LoanController {

    // @Autowired
    // private LoanService loanService;

    @Autowired
    private LoanService loanService;


    // Hämta alla lån för en specifik användare
    @GetMapping("/{userId}/loans")
    public ResponseEntity<List<LoanDTO>> getUserLoans(@PathVariable Long userId) {
        try {
            List<LoanDTO> loans = loanService.getUserLoans(userId);
            return ResponseEntity.ok(loans);
        } catch (UserNotFoundException e) {

            // Vi låter GlobalExceptionHandler hantera detta
            throw e;
        }
    }


    // Låna en bok
    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDTO createLoan(@Valid @RequestBody CreateLoanDTO createLoanDTO) {
        return loanService.createLoan(createLoanDTO);
    }

    // Att lämna tillbaka en book
    @PutMapping("/loans/{id}/return")
    public ResponseEntity<LoanDTO> returnBook(@PathVariable("id") Long loanId) {

        try {
            LoanDTO updatedLoan = loanService.returnBook(loanId);
            return ResponseEntity.ok(updatedLoan);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        }
    }

    // Förlänga lånet
    @PutMapping("/loans/{id}/extend")
    public ResponseEntity<LoanDTO> extendLoan(@PathVariable("id") Long loanId) {
        try {
            LoanDTO updatedLoan = loanService.extendLoan(loanId);
            return ResponseEntity.ok(updatedLoan);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        }
    }
}