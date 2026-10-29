package com.biblioteca.demo.loan;


import org.springframework.security.access.AccessDeniedException;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.biblioteca.demo.book.Book;
import com.biblioteca.demo.book.BookRepository;
import com.biblioteca.demo.book.BookService;
import com.biblioteca.demo.infra.exception.BusinessException;
import com.biblioteca.demo.infra.exception.ResourceNotFoundException;
import com.biblioteca.demo.user.*;

import java.util.Objects;
import jakarta.transaction.Transactional;

@Service
/*quando adicionar o Redis não esqueça o implements Serializable */
public class LoanService  {


    private final BookService    bookService;    
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;


    // Injeção de dependências do LoanRepository e LoanRepository
    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, BookService bookService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    public LoanResponseDTO findById(Long id) {
        Loan loan = loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado com o ID: " + id));

       checkOwnershipOrAdmin(loan.getUser().getId());
        
        return  toResponseDTO(loan);
                
    }

    @Transactional
public LoanResponseDTO endLoan(Long id) {
    // 1. Busca o empréstimo pelo ID (e trata o Optional)
    Loan loan = loanRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado com o ID: " + id));

    // Validação de permissão do usuário
    checkOwnershipOrAdmin(loan.getUser().getId());

    // 2. Valida se o empréstimo já foi encerrado
    if (loan.getReturned()) {
        throw new BusinessException("Este empréstimo já foi finalizado!");
    }

    // 3. Pega o livro vinculado ao empréstimo
    Book book = loan.getBook();

    // 4. Delega a atualização e a INVALIDAÇÃO DO CACHE para o BookService
    // Isso garante que o método com @CacheEvict seja disparado e a chave seja removida do Redis!
    bookService.markAsAvailableOrUnavailable(book);

    // 5. Marca o empréstimo como devolvido na entidade Loan
    loan.setReturned(true);
    Loan savedLoan = loanRepository.save(loan); 

    // 6. Retorna o DTO de resposta do empréstimo
    return toResponseDTO(savedLoan);
}

    @Transactional
    public LoanResponseDTO createLoan(CreateLoanDTO dto) {
        // 1. Instancia a nova Entity Loan e atribui os valores

        Book book = bookRepository.findById(dto.bookId())
        .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com o ID: "+dto.bookId()));
        
        User currentUser = getCurrentUser();
        // 2. Valida se o usuário já possui um empréstimo ATIVO deste mesmo livro
        boolean hasActiveLoan = loanRepository.existsByUserIdAndBookIdAndReturnedIsFalse(
                currentUser.getId(), 
                dto.bookId()
        );

        int hasEnoughLoan = loanRepository.countByUserIdAndReturnedIsFalse(
                currentUser.getId()
        );
 
        if (hasActiveLoan) {
            throw new BusinessException("Você precisa devolver este livro antes de pegá-lo emprestado novamente.");
        }
        if (hasEnoughLoan == 4) 
        throw new BusinessException("Você não pode pedir mais de 4 livros emprestado.");

        // 2. Valida se o livro está disponível para empréstimo
        if (!book.getAvailable()) {
            throw new BusinessException("Este livro já está emprestado!");
        }
 
        
        bookService.markAsAvailableOrUnavailable(book);
        bookRepository.save(book);
        // 3. Salva no banco e converte para DTO 
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(currentUser);
        loan.setReturnDate(dto.returnDate());
        Loan savedLoan = loanRepository.save(loan);
        
        return toResponseDTO(savedLoan); 
    }



    public LoanResponseDTO patchLoan(Long id, PatchLoanDTO dto){
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado com o ID: " + id));

        checkOwnershipOrAdmin(loan.getUser().getId());

        // Atualização parcial: altera apenas os campos que não forem null
        if (dto.returnDate() != null) {
            loan.setReturnDate(dto.returnDate());
        }

        Loan updatedLoan = loanRepository.save(loan);
        return toResponseDTO(updatedLoan);
    }

    public void deleteLoan(Long id) {
       Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado com o ID: " + id));
        // Se o empréstimo for excluído sem ter sido devolvido, o livro deve voltar a ficar disponível
        if (Boolean.FALSE.equals(loan.getReturned())) {
            Book book = loan.getBook();
            book.setAvailable(true);
            bookRepository.save(book);
        }

        loanRepository.deleteById(id);
    }

    // Método utilitário privado para evitar repetição do mapeamento Entity -> DTO
    private LoanResponseDTO toResponseDTO(Loan loan) {
        return new LoanResponseDTO(
                loan.getId(),
                loan.getUser(),
                loan.getBook(),
                loan.getLoanDate(),
                loan.getReturnDate(),
                loan.getReturned()
        );
    }

    public List<LoanResponseDTO> findMyLoans() {
        // 1. Pega o usuário logado do SecurityContext
        User currentUser = getCurrentUser();

        
        // 2. Se for ADMIN, traz todos. Se for USER, traz só os dele.
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return loanRepository.findAll().stream().map(this::toResponseDTO).toList();
        }

        return loanRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // Métodos Auxiliares
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkOwnershipOrAdmin(Long ownerId) {
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !Objects.equals(currentUser.getId(), ownerId)) {
            throw new AccessDeniedException("Você não tem permissão para acessar ou alterar este recurso.");
        }
    }

    
}
