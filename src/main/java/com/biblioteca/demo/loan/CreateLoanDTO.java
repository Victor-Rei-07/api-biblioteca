package com.biblioteca.demo.loan;

import java.time.Instant;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;


public record CreateLoanDTO(

    @NotNull  
    Long bookId,
    @Future
    @NotNull  
    Instant returnDate

    ) {}
