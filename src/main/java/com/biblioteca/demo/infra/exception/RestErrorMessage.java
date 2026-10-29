package com.biblioteca.demo.infra.exception;

import java.time.Instant;

public record RestErrorMessage(int status, String message,Instant timestamp) {}