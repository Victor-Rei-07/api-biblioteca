package com.biblioteca.demo.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Trata recurso não encontrado (ex: GET /users/999 que não existe) - Erro 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handle404() {
        return ResponseEntity.notFound().build();
    }
    // 3. Trata falha no login (E-mail ou senha incorretos) - Erro 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RestErrorMessage> handleBadCredentials() {
        var error = new RestErrorMessage(HttpStatus.UNAUTHORIZED.value(), "E-mail ou senha inválidos.",Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

      // 3. Trata falha no login (E-mail ou senha incorretos) - Erro 401
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RestErrorMessage> handleBusinessException(BusinessException ex) {
        var error = new RestErrorMessage(HttpStatus.CONFLICT.value(),ex.getMessage() ,Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RestErrorMessage> handleResourceException(ResourceNotFoundException ex) {
        var error = new RestErrorMessage(HttpStatus.NOT_FOUND.value(), ex.getMessage() ,Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Record auxiliar para formatar erros de validação (@Valid)
    public record ValidationErrorData(String field, String message) {
        public ValidationErrorData(org.springframework.validation.FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }


    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<RestErrorMessage> handleRefreshTokenException(RefreshTokenException ex) {
        RestErrorMessage error = new RestErrorMessage(
            HttpStatus.UNAUTHORIZED.value(), 
            ex.getMessage(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestErrorMessage> handleAccessDeniedException(
            AccessDeniedException ex, 
            HttpServletRequest request) {

        RestErrorMessage errorResponse = new RestErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                "Você não tem permissão para acessar ou alterar este recurso.",
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // Record auxiliar para erros genéricos
    

     // 4. Captura qualquer outro erro inesperado do servidor - Erro 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handle500(Exception ex) {
        var error = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro interno: " + ex.getLocalizedMessage(),Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    
    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<RestErrorMessage> handleInvalidTokenException(
            JWTVerificationException ex, 
            HttpServletRequest request) {

        RestErrorMessage errorResponse = new RestErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                "Token inválido.",
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    public record ValidationErrorResponseDTO(
    int status,
    String message,
    List<FieldErrorDTO> errors,
    Instant timestamp
    ) {
    public record FieldErrorDTO(String field, String message) {}
    }

    // 3. Captura AUTOMATICAMENTE as falhas das anotações @Valid -> HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        
        List<ValidationErrorResponseDTO.FieldErrorDTO> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ValidationErrorResponseDTO.FieldErrorDTO(error.getField(), error.getDefaultMessage()))
            .toList();

        ValidationErrorResponseDTO errorResponse = new ValidationErrorResponseDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Erro de validação nos campos da requisição",
            fieldErrors,
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
  
}