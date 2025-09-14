package com.example.library_management_v2.repository;

import com.example.library_management_v2.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Redan skapade metoder finns i JpaRepository

    // Hittar författare baserat på specifik efternamn
    List<Author> findByLastName(String lastName);

    // Hittar författare utifrån en viss text i efternamn
    List<Author> findByLastNameContainingIgnoreCase(String lastName);

    // Hitta författare baserat på förnamn, efternamn och födelseår
    List<Author> findByFirstNameAndLastNameAndBirthYear
    (
            String firstName,
            String lastName,
            Integer birthYear
    );



}