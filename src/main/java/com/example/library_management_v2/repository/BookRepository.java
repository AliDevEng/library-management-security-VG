package com.example.library_management_v2.repository;

import com.example.library_management_v2.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Söker böcker där titeln innehåller en viss sträng (case-insensitive)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Söker böcker av en specifik författare (används i sökning)
    List<Book> findByAuthorLastNameContainingIgnoreCase(String authorLastName);
}
