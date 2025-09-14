package com.example.library_management_v2.service;


// Vi skapar vi en service-klass för att hantera lånerelaterade operationer

import com.example.library_management_v2.dto.CreateLoanDTO;
import com.example.library_management_v2.dto.LoanDTO;
import com.example.library_management_v2.entity.Book;
import com.example.library_management_v2.entity.Loan;
import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.exception.BookNotAvailableException;
import com.example.library_management_v2.exception.BookNotFoundException;
import com.example.library_management_v2.exception.UserNotFoundException;
import com.example.library_management_v2.repository.BookRepository;
import com.example.library_management_v2.repository.LoanRepository;
import com.example.library_management_v2.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    // Hämta alla lån för en specifik user
    public List<LoanDTO> getUserLoans (Long userId) {

        // Vi kontrollerar om användaren finns:
        User user = userRepository.findById(userId).
                orElseThrow(() -> new UserNotFoundException ("Användare med ID: " + userId + " hittades inte"));

        // Hämta användarens lån
        List<Loan> loans = loanRepository.findByUserId(userId);


        // Konvertera och returnera lån som DTO:
        return loans.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // Vi behöver konvertera en Loan entity till LoanDTO med utökad information
    private LoanDTO convertToDTO (Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setUserId(loan.getUser().getId());
        dto.setBookId(loan.getBook().getId());
        dto.setBookTitle(loan.getBook().getTitle());
        dto.setExtended(loan.isExtended());


        // Sätt författarnamn om tillgängligt
        if (loan.getBook().getAuthor() != null) {
            String authorName =
                    loan.getBook()
                            .getAuthor()
                            .getFirstName() + " " +
                            loan.getBook().getAuthor().getLastName();
            dto.setAuthorName(authorName);
        }


        dto.setBorrowedDate(loan.getBorrowedDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnedDate(loan.getReturnedDate());
        dto.setActive(loan.isActive());


        // Beräkna och kontrollera om lånet är försenat
        if (loan.isActive() && loan.getDueDate() != null) {
            dto.setOverdue(LocalDate.now().isAfter(loan.getDueDate()));
        } else {
            dto.setOverdue(false);
        }

        return dto;
    }


    /**
     * Skapar ett nytt lån av en användare
     * createLoanDTO Data för det nya lånet (userId och bookId)
     * Returnera det skapade lånet som LoanDTO
     * Kasta UserNotFoundException om användaren inte hittas
     * Kasta BookNotFoundException om boken inte hittas
     * Kasta BookNotAvailableException om boken inte är tillgänglig
     */

    public LoanDTO createLoan(CreateLoanDTO createLoanDTO) {
        // Hämta användare och bok
        User user = userRepository.findById(createLoanDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException
                        ("Användare med ID: " + createLoanDTO.getUserId() + " hittades inte"));

        Book book = bookRepository.findById(createLoanDTO.getBookId())
                .orElseThrow(() -> new BookNotFoundException
                        ("Bok med ID: " + createLoanDTO.getBookId() + " hittades inte"));

        // Kontrollera om boken är tillgänglig
        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException
                    ("Boken \"" + book.getTitle() + "\" är inte tillgänglig för närvarande");
        }

        // Minska antalet tillgängliga exemplar
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Skapa nytt lån
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setBorrowedDate(LocalDate.now());

        // Sätt förfallodatum till 14 dagar framåt
        loan.setDueDate(LocalDate.now().plusDays(14));




        // Returnerat datum är null eftersom boken inte har återlämnats än
        loan.setReturnedDate(null);


        // Spara lånet
        Loan savedLoan = loanRepository.save(loan);

        // Returnera det skapade lånet som DTO
        return convertToDTO(savedLoan);
    }


    /**
     * Markerar ett lån som återlämnat
     * loanId ID för lånet som ska återlämnas
     * Returnera det uppdaterade lånet som LoanDTO
     * Kasta en EntityNotFoundException om lånet inte hittas
     * Kasta en IllegalStateException om lånet redan är återlämnat
     */
    public LoanDTO returnBook(Long loanId) {

        // Hämta lånet
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Lån med ID: " + loanId + " hittades inte"));


        // Kontrollera om lånet redan är återlämnat
        if (loan.getReturnedDate() != null) {
            throw new IllegalStateException("Boken är redan återlämnad (datum: " + loan.getReturnedDate() + ")");
        }


        // Markera lånet som återlämnat
        loan.setReturnedDate(LocalDate.now());


        // Öka antalet tillgängliga exemplar av boken
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);


        // Spara det uppdaterade lånet
        Loan savedLoan = loanRepository.save(loan);


        // Returnera det uppdaterade lånet som DTO
        return convertToDTO(savedLoan);
    }


    /**
     * Lånet förlängs med 14 dagar
     * loanId ID för lånet som ska förlängas
     * Returnera det uppdaterade lånet som LoanDTO
     * Kasta EntityNotFoundException om lånet inte hittas
     * Kasta IllegalStateException om lånet redan är återlämnat eller förlängt
     */
    public LoanDTO extendLoan(Long loanId) {
        // Hämta lånet
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Lån med ID: " + loanId + " hittades inte"));

        // Kontrollera om lånet redan är återlämnat
        if (loan.getReturnedDate() != null) {
            throw new IllegalStateException("Lånet kan inte förlängas eftersom boken redan är återlämnad");
        }

        // Kontrollera om lånet redan är förlängt
        if (loan.isExtended()) {
            throw new IllegalStateException("Lånet har redan förlängts en gång");
        }

        // Förläng förfallodatumet med 14 dagar
        loan.setDueDate(loan.getDueDate().plusDays(14));

        // Spara det uppdaterade lånet
        Loan savedLoan = loanRepository.save(loan);

        // Returnera det uppdaterade lånet som DTO
        return convertToDTO(savedLoan);
    }



}
