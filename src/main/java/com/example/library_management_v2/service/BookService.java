package com.example.library_management_v2.service;

import com.example.library_management_v2.dto.BookDTO;
import com.example.library_management_v2.dto.BookWithDetailsDTO;
import com.example.library_management_v2.entity.Author;
import com.example.library_management_v2.entity.Book;
import com.example.library_management_v2.repository.AuthorRepository;
import com.example.library_management_v2.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    /**
     * Hämtar alla böcker i systemet
     * @return Lista med alla böcker som BookWithDetailsDTO
     */
    public List<BookWithDetailsDTO> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(this::convertToBookWithDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * Söker böcker baserat på titel eller författarens efternamn
     * @param title Titeln att söka efter (kan vara null)
     * @param authorLastName Författarens efternamn att söka efter (kan vara null)
     * @return Lista med matchande böcker
     */
    public List<BookWithDetailsDTO> searchBooks(String title, String authorLastName) {
        List<Book> results = new ArrayList<>();

        // Om titel är angiven, sök på den
        if (title != null && !title.isEmpty()) {
            results.addAll(bookRepository.findByTitleContainingIgnoreCase(title));
        }

        // Om författarens efternamn är angivet, sök på det
        if (authorLastName != null && !authorLastName.isEmpty()) {
            results.addAll(bookRepository.findByAuthorLastNameContainingIgnoreCase(authorLastName));
        }

        // Om ingen sökning angavs, returnera alla böcker
        if ((title == null || title.isEmpty()) && (authorLastName == null || authorLastName.isEmpty())) {
            results = bookRepository.findAll();
        }

        // Konvertera resultaten till DTOs
        return results.stream()
                .distinct() // Ta bort eventuella dubbletter
                .map(this::convertToBookWithDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * Skapar en ny bok i systemet
     * @param bookDTO Data för den nya boken
     * @return Den skapade boken som BookDTO
     */
    public BookDTO createBook(BookDTO bookDTO) {
        // Konvertera DTO till entity
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setPublicationYear(bookDTO.getPublicationYear());
        book.setAvailableCopies(bookDTO.getAvailableCopies());
        book.setTotalCopies(bookDTO.getTotalCopies());

        // Hämta författaren om ett ID är angivet
        if (bookDTO.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDTO.getAuthorId())
                    .orElseThrow(() -> new RuntimeException("Författaren hittades inte med ID: " + bookDTO.getAuthorId()));
            book.setAuthor(author);
        }

        // Spara boken i databasen
        Book savedBook = bookRepository.save(book);

        // Konvertera och returnera den sparade boken som DTO
        return convertToBookDTO(savedBook);
    }

    /**
     * Konverterar en Book entity till BookDTO
     * @param book Book entity att konvertera
     * @return BookDTO
     */
    private BookDTO convertToBookDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setTotalCopies(book.getTotalCopies());

        if (book.getAuthor() != null) {
            dto.setAuthorId(book.getAuthor().getId());
        }

        return dto;
    }

    /**
     * Konverterar en Book entity till BookWithDetailsDTO
     * @param book Book entity att konvertera
     * @return BookWithDetailsDTO med författarinformation
     */
    private BookWithDetailsDTO convertToBookWithDetailsDTO(Book book) {
        BookWithDetailsDTO dto = new BookWithDetailsDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setTotalCopies(book.getTotalCopies());

        if (book.getAuthor() != null) {
            dto.setAuthorFirstName(book.getAuthor().getFirstName());
            dto.setAuthorLastName(book.getAuthor().getLastName());
        }

        return dto;
    }
}