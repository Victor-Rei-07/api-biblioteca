package com.biblioteca.demo.book;

import com.biblioteca.demo.author.*;

public record BookResponseDTO(
    Long id,
    Author author,
    String title,
    String isbn,
    Boolean available
){}
