package com.example.library_management_v2.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

// Vi skapar en loan-entitet som liknar vår databas
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "borrowed_date")
    private LocalDate borrowedDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;



    // Tom konstruktor som krävs av Spring
    public Loan() {}

    // Getters och setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getBorrowedDate() {
        return borrowedDate;
    }

    public void setBorrowedDate(LocalDate borrowedDate) {
        this.borrowedDate = borrowedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }



    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    // Hjälpmetod för att kontrollera om lånet är aktivt (inte återlämnat)
    public boolean isActive() {
        return returnedDate == null;
    }

    // Hjälpmetod för att kontrollera om lånet är försenat
    public boolean isOverdue() {
        return isActive() && LocalDate.now().isAfter(dueDate);
    }


    public boolean isExtended() {

        // Om dueDate är mer än 14 dagar efter borrowedDate, anses lånet förlängt

        return borrowedDate != null && dueDate != null &&
                dueDate.isAfter(borrowedDate.plusDays(14));
    }
}