package com.biblioteca.demo.domain.user;


public enum UserRole {
    ADMIN("admin"), // Criação do objeto ADMIN passando no seu construtor a string "admin"
    USER("user");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}