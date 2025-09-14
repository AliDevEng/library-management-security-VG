package com.example.library_management_v2.repository;

import com.example.library_management_v2.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Hämta alla lån för en specifik användare
    List<Loan> findByUserId(Long userId);

    // Hämta aktiva lån för en specifik användare
    List<Loan> findByUserIdAndReturnedDateIsNull (Long userId);


}
