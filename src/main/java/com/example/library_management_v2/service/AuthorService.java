// src/main/java/com/example/librarymangementv2/service/AuthorService.java
package com.example.library_management_v2.service;

import com.example.library_management_v2.dto.AuthorDTO;
import com.example.library_management_v2.dto.CreateAuthorDTO;
import com.example.library_management_v2.entity.Author;
import com.example.library_management_v2.exception.AuthorNotFoundException;
import com.example.library_management_v2.exception.DuplicateAuthorException;
import com.example.library_management_v2.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    /**
     * Hämtar alla författare i systemet
     * @return Lista med alla författare som AuthorDTO
     */

    public List<AuthorDTO> getAllAuthors() {
        // Hämta alla författare från databasen
        List<Author> authors = authorRepository.findAll();

        // Konvertera och returnera författarna som DTOs
        return authors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }




    // Hitta författare baserat på efternamn
    public List<AuthorDTO> getAuthorsByLastName(String lastName) {


        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Efternamn kan inte vara tomt");
        }

        // Hämta författare med exakt efternamn
        List<Author> authors = authorRepository.findByLastName(lastName);

        // Om inga träffar, prova med ungefärlig matchning
        if (authors.isEmpty()) {
            authors = authorRepository.findByLastNameContainingIgnoreCase(lastName);
        }

        // Om fortfarande inga träffar, kasta exception
        if (authors.isEmpty()) {
            throw new AuthorNotFoundException("Inga författare hittades med efternamnet: " + lastName);
        }

        // Konvertera och returnera författarna som DTOs
        return authors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Skapa en ny författare
    public AuthorDTO createAuthor(CreateAuthorDTO createAuthorDTO) {
        // Validera indata (ytterligare validering utöver annotations)
        if (createAuthorDTO.getBirthYear() != null && createAuthorDTO.getBirthYear() > java.time.Year.now().getValue()) {
            throw new IllegalArgumentException("Födelseår kan inte vara i framtiden");
        }

        // Kontrollera om författaren redan finns (baserat på namn och födelseår)
        List<Author> existingAuthors = authorRepository.findByFirstNameAndLastNameAndBirthYear(
                createAuthorDTO.getFirstName(),
                createAuthorDTO.getLastName(),
                createAuthorDTO.getBirthYear());

        if (!existingAuthors.isEmpty()) {
            throw new DuplicateAuthorException("En författare med detta namn och födelseår finns redan");
        }

        // Konvertera DTO till entity
        Author author = new Author();
        author.setFirstName(createAuthorDTO.getFirstName());
        author.setLastName(createAuthorDTO.getLastName());
        author.setBirthYear(createAuthorDTO.getBirthYear());
        author.setNationality(createAuthorDTO.getNationality());

        // Spara författaren i databasen
        Author savedAuthor = authorRepository.save(author);

        // Konvertera och returnera den sparade författaren som DTO
        return convertToDTO(savedAuthor);
    }


    // Konvertera en Author entity till AuthorDTO
    // Return ger konverterad AuthorDTO
    private AuthorDTO convertToDTO(Author author) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setFirstName(author.getFirstName());
        dto.setLastName(author.getLastName());
        dto.setBirthYear(author.getBirthYear());
        dto.setNationality(author.getNationality());
        return dto;
    }



}