package com.biblioteca.demo.loan;

import java.time.Instant;

import com.biblioteca.demo.user.User;
import com.biblioteca.demo.book.Book;

public record LoanResponseDTO(
    Long id,
    User user,
    Book book,
    Instant loandDate,
    Instant returnDate,
    Boolean returned
) {}
