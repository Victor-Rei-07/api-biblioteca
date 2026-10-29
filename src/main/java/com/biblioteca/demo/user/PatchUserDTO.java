package com.biblioteca.demo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record PatchUserDTO(

    @Size(max = 100, message = "Nome inválido")
    String name,

    @Schema(example = "dev@biblioteca.com", description = "Novo e-mail do usuário")
    @Email(message = "Email inválido")
    @Pattern(regexp = ".*\\S.*", message = "O email não pode ser vazio ou conter apenas espaços")
    @Size(max = 256)
    String email
) {}
