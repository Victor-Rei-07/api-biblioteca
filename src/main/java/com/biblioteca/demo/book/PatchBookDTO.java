package com.biblioteca.demo.book;

import jakarta.validation.constraints.Size;

public record PatchBookDTO(
    @Size(max = 200)
    String title,

    @Size(min = 10, max = 13)
    String isbn,

    Boolean available
) {}

