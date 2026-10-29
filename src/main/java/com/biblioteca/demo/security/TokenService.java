package com.biblioteca.demo.security;

import com.biblioteca.demo.user.User;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {
 
    // Sintaxe correta para ler a propriedade ou usar a chave padrao
    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) { 
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            return Jwts.builder()
                    .issuer("biblioteca-api")
                    .subject(user.getEmail())
                    .claim("role", user.getRole().name())

                    .issuedAt(Date.from(Instant.now())) 
                    .expiration(Date.from(genExpirationDate())) 
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

    public String validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            return Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return "";
        }
    }

 private Instant genExpirationDate() {
    return Instant.now().plus(2, ChronoUnit.HOURS);
}


}