package com.example.library_management_v2.repository;

import com.example.library_management_v2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Hitta användare baserat på Email
    Optional<User> findByEmail (String email);

    // Praktisk för att kolla om Email redan finns
    //boolean existByEmail(String email);
}
