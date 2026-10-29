package com.biblioteca.demo.user;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.biblioteca.demo.domain.user.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore; 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; 
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role") 
    private UserRole role;

    public User() {}

    public User(String email, String password, UserRole role, String name) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    // --- MÉTODOS DO USERDETAILS (Todos com @JsonIgnore para o Jackson não tentar ler/escrever propriedades calculadas) --- 

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return this.email;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore // <-- Adicionado para evitar o erro de setterless property no 'enabled'
    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- GETTERS E SETTERS (Obrigatórios para o Jackson preencher todos os campos) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // <-- Adicionado

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
} 