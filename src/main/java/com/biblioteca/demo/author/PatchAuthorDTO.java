package com.biblioteca.demo.author;

import jakarta.validation.constraints.Size;

public record PatchAuthorDTO(
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    String name,

    @Size(max = 256, message = "A bio deve ter no máximo 256 caracteres")
    String bio
) {}
