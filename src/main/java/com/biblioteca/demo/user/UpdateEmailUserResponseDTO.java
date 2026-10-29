package com.biblioteca.demo.user;

public record UpdateEmailUserResponseDTO(
    UserResponseDTO user,
    String token
) {}
