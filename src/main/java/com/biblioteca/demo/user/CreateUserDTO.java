package com.biblioteca.demo.user;

import com.biblioteca.demo.domain.user.UserRole;

import jakarta.validation.constraints.*;

public record CreateUserDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    String email,

    @NotBlank(message =  "A senha é obrigatória")
    String password,

    @NotNull UserRole role 
) {}
