package com.biblioteca.demo.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuthorDTO( 
@NotBlank(message = "O nome é obrigatório") 
String name, 

@Size(max = 256) 
String bio 
) {}