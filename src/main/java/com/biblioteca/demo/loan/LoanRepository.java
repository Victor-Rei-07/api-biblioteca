package com.biblioteca.demo.loan;


import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface LoanRepository extends JpaRepository<Loan, Long>  {

    List<Loan> findByUserId(Long id);
    // Retorna true se existir um empréstimo deste usuário + livro onde o returned ainda é false (ativo)
    boolean existsByUserIdAndBookIdAndReturnedIsFalse(Long userId, Long bookId);
    boolean existsByUserIdAndReturnedIsFalse(Long userId);
    @Modifying // A query agora vai fazer exatamente o que o @Query abaixo diz
    @Query("DELETE FROM Loan l WHERE l.user.id = :userId") // Executa essa query agora
    void deleteByUserId(Long userId);
    boolean existsByBookId(Long bookId);
    int countByUserIdAndReturnedIsFalse(Long userId); 

    @Override
    @EntityGraph(attributePaths = {
        "user",
        "book",
        "book.author"
    })
    List<Loan> findAll();  
}
