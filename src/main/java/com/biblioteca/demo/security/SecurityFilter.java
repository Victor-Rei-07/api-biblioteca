package com.biblioteca.demo.security;

import com.biblioteca.demo.user.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserService userService; // Injeta o UserService em vez do Repository

    public SecurityFilter(TokenService tokenService, @Lazy UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var token = recoverToken(request); // Obtém o JWT

        if (token != null) {
            var email = tokenService.validateToken(token); // Obtém o email caso o token esteja válido

            if (email != null && !email.isEmpty()) {  
        UserDetails user = userService.findByEmail(email); 

        if (user != null) {
            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
        }

        // Continua o fluxo da requisição
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization"); // Pega o header authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) { // Se não possuir o bearer ou não existir o authorization, retorna null
            return null;
        }
        return authHeader.replace("Bearer ", ""); // Retorna o JWT
    }
}