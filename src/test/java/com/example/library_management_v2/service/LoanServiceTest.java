package com.example.library_management_v2.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.example.library_management_v2.dto.CreateLoanDTO;
import com.example.library_management_v2.dto.LoanDTO;
import com.example.library_management_v2.entity.Author;
import com.example.library_management_v2.entity.Book;
import com.example.library_management_v2.entity.Loan;
import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.exception.BookNotAvailableException;
import com.example.library_management_v2.exception.BookNotFoundException;
import com.example.library_management_v2.exception.UserNotFoundException;
import com.example.library_management_v2.repository.BookRepository;
import com.example.library_management_v2.repository.LoanRepository;
import com.example.library_management_v2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    // Mock-objekt - "fejk" versioner av vår repositories
    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    // Den verkliga service vi vill testa
    @InjectMocks
    private LoanService loanService;


    // Testdata som vi kommer använda i flera tester
    private User testUser;
    private Book testBook;
    private Author testAuthor;
    private CreateLoanDTO createLoanDTO;

    @BeforeEach // Vi säkerställer att varje test börjar med "färsk" objekt
    public void setUp () {

        // Skapa en test-användare
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("test");
        testUser.setLastName("User");
        testUser.setEmail("test@gmail.com");
        testUser.setRegistrationDate(LocalDate.now());

        // Skapa en test-författare
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("Test");
        testAuthor.setLastName("Author");
        testAuthor.setBirthYear(1970);

        // Skapa en test-book med tillgängliga copies
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAvailableCopies(5);
        testBook.setTotalCopies(10);
        testBook.setAuthor(testAuthor);


        // Skapa en DTO för lån-frågor
        createLoanDTO = new CreateLoanDTO();
        createLoanDTO.setUserId(1L);
        createLoanDTO.setBookId(1L);


    }


    // Nu går vi över till test
    @Test
    @DisplayName("Vi skpar ett nytt bok-lån när User och bok finns samt boken är tillgänlig")
    public void testCreateLoan_Success () {

        // Arrange: Sätt upp vad våra MOCK-objekt ska returnera
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));


        // Vi skapar ett förväntat lån som vårt Mock-Repository ska returnera vid save()

        Loan expectedLoan = new Loan();

        expectedLoan.setId(1L);         // Vi simulerar att vår databas genereart ett ID
        expectedLoan.setUser(testUser);
        expectedLoan.setBook(testBook);
        expectedLoan.setBorrowedDate(LocalDate.now());
        expectedLoan.setDueDate(LocalDate.now().plusDays(14));      // Vi bestämde 14 dagar innan
        expectedLoan.setReturnedDate(null);     // Nytt lån är inte lämnat


        // Vi programmerar vad Mock-Repo ska returnera
        when(loanRepository.save(any(Loan.class))).thenReturn(expectedLoan);
        // any(Loan.class) betyder: När någon anropar save() med vilket Loan-objekt som helst
        // Mindre intressant vilket Loan-objekt som skickas in
        // Vi vill bara att Mock:en ska returnera vårt förberedda "expectedLoan"


        // Act: Vi anropar den metod vi vill testa
        LoanDTO result = loanService.createLoan(createLoanDTO);



        // Assert: Kontrollera alla delar i lånet
        // Vi måste verifiera att resultatet är som förväntat
        assertNotNull(result, "Resultatet ska inte vara null");
        assertEquals(1L, result.getId(), "Lån-ID ska matcha förväntat ID");
        assertEquals(1L, result.getUserId(), "User-ID ska matcha");
        assertEquals(1L, result.getBookId(), "Bok-ID ska matcha");

        assertEquals("Test Book", result.getBookTitle(), "Matcha vår boktitel");
        assertEquals("Test Author", result.getAuthorName(), "Matcha förnamn");
        assertEquals(LocalDate.now(), result.getBorrowedDate(), "Lånedatum ska vara dagens datum");
        assertEquals(LocalDate.now().plusDays(14), result.getDueDate(), "Förfallodatum 14 dagar senare");

        assertNull(result.getReturnedDate(), "Return-date ska vara null vid nytt boklån");
        assertTrue(result.isActive(), "Lånet ska vara aktivt");
        assertFalse(result.isOverdue(), "Nytt boklån ska inte avra försenat");


        // Vi måste verifiera att repositories anropades på rätt sätt (Behavior Testing)
        verify(userRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(testBook);      // Boken ska sparas med uppdaterade tillgängliga exemplar
        verify(loanRepository).save(any(Loan.class));


        // Vi måste kontrollera att bokens tillgängliga antal minskade
        assertEquals(4, testBook.getAvailableCopies(), "Tillgängliga copies måste minska med 1");

    }


    @Test
    @DisplayName("Ska kasta UserNotFoundException när användaren inte finns")
    public void testCreateLoan_UserNotFound () {

        //Arrange
        // Vi simulerar att användare inte finns
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        // Optional.empty() >>> Låtsas att du inte hittar användare med ID 1

        // Act
        UserNotFoundException exception = assertThrows (
                UserNotFoundException.class,
                () -> loanService.createLoan(createLoanDTO),
                "Sak kasta UserNotFoundException när användare inte finns"
        );

        // Asseert
        // Vi ger ett konkrett meddelande om vad som behöver åtgärdas
        assertEquals("Användare med ID: 1 hittades inte", exception.getMessage());


        // Vi måste verifiera att inga andra repositories anropas
        verify(userRepository).findById(1L);
        verify(bookRepository, never()).findById(any());
        verify(loanRepository, never()).save(any());
        verify(bookRepository, never()).save(any());

        // never() >>> Det har inte hänt.
        // Viktigt att kontrollera att när anv'ndare inte hittades slutade systemet direkt att arbeta.

    }


    @Test
    @DisplayName("Ska kasta BookNotFoundException när användaren inte finns")
    public void testCreateLoan_BookNotFound () {

        //Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Vi simulerar att boken inte finns
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        // Optional.empty() >>> Låtsas att du inte hittar book med ID 1

        // Act
        BookNotFoundException exception = assertThrows (
                BookNotFoundException.class,
                () -> loanService.createLoan(createLoanDTO),
                "Ska kasta BookNotFoundException när boken inte finns"
        );

        // Assert
        // Vi ger ett konkrett meddelande om vad som behöver åtgärdas
        assertEquals("Bok med ID: 1 hittades inte", exception.getMessage());


        // Vi måste verifiera vår anrop
        verify(userRepository).findById(1L);            // Detta SKULLE ha hänt
        verify(bookRepository).findById(1L);            // Detta SKULLE ha hänt

        verify(loanRepository, never()).save(any());    // Detta skulle ALDRIG ha hänt
        // never() >>> Det har inte hänt.

    }


    @Test
    @DisplayName("Ska kasta BookNotAvailableException när användaren inte finns")
    public void testCreateLoan_BookNotAvailable () {

        //Arrange
        testBook.setAvailableCopies(0);     // Det ska inte finnas några exemplar att låna ut

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));


        // Act
        BookNotAvailableException exception = assertThrows (
                BookNotAvailableException.class,
                () -> loanService.createLoan(createLoanDTO),
                "Ska kasta BookNotAvailableException när boken inte är tillgänglig"
        );

        // Vi ger ett konkrett meddelande om vad som behöver åtgärdas
        assertTrue(exception.getMessage().contains("Test Book"));
        assertTrue(exception.getMessage().contains("inte tillgänglig"));
        // contains() >>>> Felmeddelandet kan ändras men måste innehålla "bokens namn" samt "inte tillgänglig"


        // Assert
        // Verifiera att boken inte sparades eftersom transaktionen ska avbrytas
        verify(userRepository).findById(1L);            // Detta SKULLE ha hänt
        verify(bookRepository).findById(1L);            // Detta SKULLE ha hänt
        verify(bookRepository, never()).save(any());    // KRITISK kontroll
        verify(loanRepository, never()).save(any());    // Detta skulle ALDRIG ha hänt
        // never() >>> Det har inte hänt.

    }

    // Viktig test för att kontrollera vad händer när 1 tillgänglig exemplat blir 0
    @Test
    @DisplayName("Ska hantera gränsfall när boken har exakt ett exemplar kvar")
    public void testCreateLoan_LastAvailableCopy() {
        // Arrange
        // Sätt bokens tillgängliga exemplar till 1
        testBook.setAvailableCopies(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));


        // Skapa det förväntade lånet som ska returneras
        Loan expectedLoan = new Loan();
        expectedLoan.setId(1L);
        expectedLoan.setUser(testUser);
        expectedLoan.setBook(testBook);
        expectedLoan.setBorrowedDate(LocalDate.now());
        expectedLoan.setDueDate(LocalDate.now().plusDays(14));

        when(loanRepository.save(any(Loan.class))).thenReturn(expectedLoan);

        // Act
        LoanDTO result = loanService.createLoan(createLoanDTO);

        // Assert
        assertNotNull(result, "Lånet ska skapas även med sista exemplaret");
        assertEquals(0, testBook.getAvailableCopies(), "Tillgängliga exemplar ska vara 0 efter lånet");

        verify(bookRepository).save(testBook);
        verify(loanRepository).save(any(Loan.class));
    }
}
