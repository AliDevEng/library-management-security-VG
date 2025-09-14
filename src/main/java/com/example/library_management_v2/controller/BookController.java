package com.example.library_management_v2.controller;

import com.example.library_management_v2.dto.BookDTO;
import com.example.library_management_v2.dto.BookWithDetailsDTO;
import com.example.library_management_v2.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * Hämtar alla böcker
     * Returnera en lista med alla böcker som BookWithDetailsDTO
     */
    @GetMapping
    public List<BookWithDetailsDTO> getAllBooks() {
        return bookService.getAllBooks();
    }

    /**
     * Söker böcker baserat på titel eller författare
     * title Titeln att söka efter (frivillig)
     * author Författarens efternamn att söka efter (frivillig)
     * Vi får en lista med matchande böcker
     */
    @GetMapping("/search")
    public List<BookWithDetailsDTO> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author) {
        return bookService.searchBooks(title, author);
    }

    /**
     * Skapa en ny bok
     * bookDTO Data för den nya boken
     * Vi får den skapade boken som BookDTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO createBook(@RequestBody BookDTO bookDTO) {
        return bookService.createBook(bookDTO);
    }
}