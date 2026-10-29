package com.biblioteca.demo.infra.exception;

public class ResourceNotFoundException extends RuntimeException {
 public ResourceNotFoundException(String message) {
        super(message);
    }
}
