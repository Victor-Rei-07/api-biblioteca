package com.biblioteca.demo.book;

import jakarta.validation.constraints.*;

public record CreateBookDTO(
    @NotBlank(message = "O título é obrigatório")
    @Size(max = 200)
    String title,

    @Size(min = 10, max = 13)
    String isbn,

    @NotNull(message = "O id do autor é obrigatório")
    @Positive
    Long authorId
) {}
