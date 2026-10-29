package com.biblioteca.demo.refreshtoken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.biblioteca.demo.infra.exception.RefreshTokenException;
import com.biblioteca.demo.user.*;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    
    private RefreshTokenRepository refreshTokenRepository;


    private UserRepository userRepository;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
       this.userRepository = userRepository;
       this.refreshTokenRepository = refreshTokenRepository;
    }

    // Cria e salva um novo Refresh Token para o usuário
    @Transactional
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email);

        // Deleta tokens antigos do usuário se existirem
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS)); // Válido por 7 dias
        refreshToken.setToken(UUID.randomUUID().toString()); // Gera uma string aleatória

        return refreshTokenRepository.save(refreshToken); // Salva no banco
    }

    // Verifica se o token expirou no banco
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token); //Deleta o token expirado
           throw new RefreshTokenException("Refresh token expirado. Por favor, faça login novamente.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token); //retorna o token procurado
    }

    @Transactional
    public void deleteByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenException("Refresh token inválido ou não encontrado."));
        
        refreshTokenRepository.delete(refreshToken);
    }
}