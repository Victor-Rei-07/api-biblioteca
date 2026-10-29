package com.biblioteca.demo.security;

import com.biblioteca.demo.infra.exception.RefreshTokenException;
import com.biblioteca.demo.refreshtoken.*;
import com.biblioteca.demo.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints para gerenciamento de logins e tokens")
public class AuthController { 

    // Os objetos vêm antes do construtor
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService,RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    } 

    @Operation(
        summary = "Inicia uma sessão",
        description = "Inicia uma nova sessão temporária na aplicação e retorna os tokens através da autenticação de email e senha."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Sessão iniciada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        )
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {
    
       var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var user = (User) auth.getPrincipal();
        
        // 1. Gera o JWT curto
        var accessToken = tokenService.generateToken(user);
        
        // 2. Gera o Refresh Token longo e salva no banco
        var refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return ResponseEntity.ok(new TokenResponseDTO(accessToken, refreshToken.getToken()));
}

    
    @Operation(
        summary = "Loga novamente",   
        description = "Manda um refresh token para renovar um access token, sem o usuário precisar colocar a senha de novo."
    )
    @ApiResponses(value = { 
        @ApiResponse(
            responseCode = "200", 
            description = "Sessão reiniciada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Gera um NOVO Access Token sem pedir a senha!
                    String newAccessToken = tokenService.generateToken(user);
                    return ResponseEntity.ok(new TokenResponseDTO(newAccessToken, request.refreshToken()));
                })
                .orElseThrow(() -> new RefreshTokenException("Refresh token inválido ou não encontrado no banco de dados."));
    }

    
    @Operation(
        summary = "Finaliza uma sessão",
        description = "Desloga uma sessão da aplicação e exclui o refresh token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Sessão excluída com sucesso",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        )
    })
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequestDTO request) {
        refreshTokenService.deleteByToken(request.refreshToken());

        return ResponseEntity.noContent().build(); // Retorna HTTP 204 No Content
    }

}