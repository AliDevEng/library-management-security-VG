package com.example.library_management_v2.controller;

import com.example.library_management_v2.config.TestDatabaseConfig;
import com.example.library_management_v2.dto.CreateLoanDTO;
import com.example.library_management_v2.entity.Author;
import com.example.library_management_v2.entity.Book;
import com.example.library_management_v2.entity.User;
import com.example.library_management_v2.repository.AuthorRepository;
import com.example.library_management_v2.repository.BookRepository;
import com.example.library_management_v2.repository.LoanRepository;
import com.example.library_management_v2.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Starta hela Spring Boot-applikationen för testet
@SpringBootTest
// Aktivera MockMvc för att simulera HTTP-anrop
@AutoConfigureMockMvc
// Använd test-profilen som vi konfigurerade i application-test.properties
@ActiveProfiles("test")


@Import(TestDatabaseConfig.class)
// Importerar vår explicita testkonfiguration

// Varje test körs i en transaktion som rullas tillbaka efter testet
@Transactional
public class LoanControllerIntegrationTest {

    // MockMvc låter oss simulera HTTP-anrop utan att starta en riktig webserver
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper hjälper oss att konvertera objekt till JSON och vice versa
    @Autowired
    private ObjectMapper objectMapper;

    // Riktiga repositories som kommer att prata med vår testdatabas
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private LoanRepository loanRepository;

    // Testdata som vi kommer att skapa i databasen före varje test
    private User testUser;
    private Book testBook;
    private Author testAuthor;

    @BeforeEach
    public void setUp() {
        // Rensa databasen innan varje test (säkerhetsmässigt)
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        userRepository.deleteAll();

        // Skapa och spara en testförfattare i databasen
        testAuthor = new Author();
        testAuthor.setFirstName("Integration");
        testAuthor.setLastName("TestAuthor");
        testAuthor.setBirthYear(1980);
        testAuthor.setNationality("Swedish");
        testAuthor = authorRepository.save(testAuthor);

        // Skapa och spara en testanvändare i databasen
        testUser = new User();
        testUser.setFirstName("Integration");
        testUser.setLastName("TestUser");
        testUser.setEmail("integration@test.com");
        testUser.setPassword("password123");
        testUser.setRegistrationDate(LocalDate.now());
        testUser = userRepository.save(testUser);

        // Skapa och spara en testbok i databasen
        testBook = new Book();
        testBook.setTitle("Integration Test Book");
        testBook.setPublicationYear(2024);
        testBook.setAvailableCopies(5);
        testBook.setTotalCopies(10);
        testBook.setAuthor(testAuthor);
        testBook = bookRepository.save(testBook);
    }

    @Test
    @DisplayName("POST /loans ska skapa ett nytt lån och returnera 201 Created")
    public void testCreateLoan_Success() throws Exception {
        // Arrangera: Skapa en DTO för vårt låneförfrågan
        CreateLoanDTO createLoanDTO = new CreateLoanDTO();
        createLoanDTO.setUserId(testUser.getId());
        createLoanDTO.setBookId(testBook.getId());

        // Konvertera DTO till JSON-sträng
        String jsonRequest = objectMapper.writeValueAsString(createLoanDTO);

        // Agera och bekräfta: Utför POST-anrop och verifiera svaret
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // Verifiera HTTP-statuskod
                .andExpect(status().isCreated())
                // Verifiera att svaret är JSON
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verifiera specifika fält i JSON-svaret
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId", equalTo(testUser.getId().intValue())))
                .andExpect(jsonPath("$.bookId", equalTo(testBook.getId().intValue())))
                .andExpect(jsonPath("$.bookTitle", equalTo("Integration Test Book")))
                .andExpect(jsonPath("$.authorName", equalTo("Integration TestAuthor")))
                .andExpect(jsonPath("$.borrowedDate", equalTo(LocalDate.now().toString())))
                .andExpect(jsonPath("$.dueDate", equalTo(LocalDate.now().plusDays(14).toString())))
                .andExpect(jsonPath("$.returnedDate", nullValue()))
                .andExpect(jsonPath("$.active", equalTo(true)))
                .andExpect(jsonPath("$.overdue", equalTo(false)));

        // Ytterligare verifiering: Kontrollera att lånet faktiskt skapades i databasen
        assertEquals(1, loanRepository.count(), "Ett lån ska ha skapats i databasen");

        // Verifiera att bokens tillgängliga exemplar minskade
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(4, updatedBook.getAvailableCopies(),
                "Bokens tillgängliga exemplar ska ha minskat från 5 till 4");
    }

    @Test
    @DisplayName("POST /loans ska returnera 404 när användaren inte finns")
    public void testCreateLoan_UserNotFound() throws Exception {
        // Arrangera: Skapa en förfrågan med ett användar-ID som inte finns
        CreateLoanDTO createLoanDTO = new CreateLoanDTO();
        createLoanDTO.setUserId(999L); // Detta ID finns inte i databasen
        createLoanDTO.setBookId(testBook.getId());

        String jsonRequest = objectMapper.writeValueAsString(createLoanDTO);

        // Agera och bekräfta: Förvänta oss en 404-fel
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", containsString("Användare med ID: 999 hittades inte")));

        // Verifiera att inget lån skapades
        assertEquals(0, loanRepository.count(), "Inget lån ska ha skapats när användaren inte finns");

        // Verifiera att bokens tillgängliga exemplar inte ändrades
        Book unchangedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(5, unchangedBook.getAvailableCopies(),
                "Bokens tillgängliga exemplar ska vara oförändrade");
    }

    @Test
    @DisplayName("POST /loans ska returnera 404 när boken inte finns")
    public void testCreateLoan_BookNotFound() throws Exception {
        // Arrangera: Skapa en förfrågan med ett bok-ID som inte finns
        CreateLoanDTO createLoanDTO = new CreateLoanDTO();
        createLoanDTO.setUserId(testUser.getId());
        createLoanDTO.setBookId(999L); // Detta ID finns inte i databasen

        String jsonRequest = objectMapper.writeValueAsString(createLoanDTO);

        // Agera och bekräfta
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Bok med ID: 999 hittades inte")));

        // Verifiera att inget lån skapades
        assertEquals(0, loanRepository.count(), "Inget lån ska ha skapats när boken inte finns");
    }

    @Test
    @DisplayName("POST /loans ska returnera 400 när boken inte är tillgänglig")
    public void testCreateLoan_BookNotAvailable() throws Exception {
        // Arrangera: Sätt bokens tillgängliga exemplar till 0
        testBook.setAvailableCopies(0);
        bookRepository.save(testBook);

        CreateLoanDTO createLoanDTO = new CreateLoanDTO();
        createLoanDTO.setUserId(testUser.getId());
        createLoanDTO.setBookId(testBook.getId());

        String jsonRequest = objectMapper.writeValueAsString(createLoanDTO);

        // Agera och bekräfta
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("inte tillgänglig")));

        // Verifiera att inget lån skapades
        assertEquals(0, loanRepository.count(), "Inget lån ska ha skapats när boken inte är tillgänglig");
    }

    @Test
    @DisplayName("POST /loans ska returnera 400 när obligatoriska fält saknas")
    public void testCreateLoan_ValidationError() throws Exception {
        // Arrangera: Skapa en tom DTO (saknar userId och bookId)
        CreateLoanDTO createLoanDTO = new CreateLoanDTO();
        // Inte sätta userId eller bookId för att trigga validering

        String jsonRequest = objectMapper.writeValueAsString(createLoanDTO);

        // Agera och bekräfta
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verifiera att valideringsfel returneras för båda fälten
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.bookId", notNullValue()));

        // Verifiera att inget lån skapades
        assertEquals(0, loanRepository.count(), "Inget lån ska ha skapats vid valideringsfel");
    }

    @Test
    @DisplayName("POST /loans ska hantera gränsfall med sista tillgängliga exemplaret")
    public void testCreateLoan_LastAvailableCopy() throws Exception {
        // Arrangera: Sätt bokens tillgängliga exemplar till 1
        testBook.setAvailableCopies(1);
        bookRepository.save(testBook);

        CreateLoanDTO createLoanDTO = new CreateLoanDTO();
        createLoanDTO.setUserId(testUser.getId());
        createLoanDTO.setBookId(testBook.getId());

        String jsonRequest = objectMapper.writeValueAsString(createLoanDTO);

        // Agera och bekräfta
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        // Verifiera att lånet skapades
        assertEquals(1, loanRepository.count(), "Lånet ska ha skapats även med sista exemplaret");

        // Verifiera att boken nu har 0 tillgängliga exemplar
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(0, updatedBook.getAvailableCopies(),
                "Boken ska nu ha 0 tillgängliga exemplar");
    }
}