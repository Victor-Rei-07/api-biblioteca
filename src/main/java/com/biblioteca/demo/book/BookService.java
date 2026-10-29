package com.biblioteca.demo.book;



import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import com.biblioteca.demo.author.Author;
import com.biblioteca.demo.author.AuthorRepository;
import com.biblioteca.demo.infra.exception.BusinessException;
import com.biblioteca.demo.infra.exception.ResourceNotFoundException;
import com.biblioteca.demo.loan.LoanRepository;

import org.springframework.data.jpa.domain.Specification;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LoanRepository loanRepository;

    // Injeção de dependências do BookRepository e AuthorRepository
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository,LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.loanRepository = loanRepository;
    }

    public List<BookResponseDTO> findAll() {
        return bookRepository.findAll()
                .stream() // Cria uma "esteira" e bota os livros nela
                .map(this::toResponseDTO) //coloca cada item da esteira em um DTO
                .toList(); // retorna todos os DTOs
    }

    public Page<BookResponseDTO> findAllPaged(String search, Boolean available ,Pageable pageable) {
        Specification<Book> spec = BookSpecifications.withFilter(search,available); //constrói o "WHERE"
        return bookRepository.findAll(spec, pageable).map(this::toResponseDTO);  
    }


    // Procura primeiro no Redis chave 'books::6'. 
    // Se não achar, executa a consulta SQL no Postgres e salva no Redis.
    @Cacheable(value = "books", key = "#id")
    public BookResponseDTO findById(Long id) {
        return bookRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com o ID: " + id));
    }

    public BookResponseDTO createBook(CreateBookDTO dto) {
        // 1. Busca o autor no banco usando o authorId que veio no DTO
        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Autor não encontrado com o ID: " + dto.authorId()));

        // 2. Instancia a nova Entity Book e atribui os valores
        Book book = new Book();
        book.setTitle(dto.title());
        book.setIsbn(dto.isbn());
        book.setAuthor(author);

        // 3. Salva no banco e converte para DTO
        Book savedBook = bookRepository.save(book);
        return toResponseDTO(savedBook);
    }

    public BookResponseDTO updateBook(Long id, PatchBookDTO dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com o ID: " + id));

        // No PUT/Atualização total, substitui todos os valores
        book.setTitle(dto.title());
        book.setIsbn(dto.isbn());
        if (dto.available() != null) {
            book.setAvailable(dto.available());
        }

        Book updatedBook = bookRepository.save(book);
        return toResponseDTO(updatedBook);
    }

    /**
     * O @CacheEvict limpa/apaga a chave específica do livro no Memurai/Redis.
     * 
     * - value = "books": O namespace/gaveta do cache de livros.
     * - key = "#id": Extrai o ID do livro passado no parâmetro para formar a chave (ex: "books::15").
     * - beforeInvocation = false (Padrão): A chave SÓ é apagada se a transação no banco der SUCESSO! 
     *   Se ocorrer uma exceção na devolução, o cache não é afetado.
     */
    @CacheEvict(value = "books", key = "#book.id") 
    @Transactional
    public void markAsAvailableOrUnavailable(Book book) {
        book.setAvailable(!book.isAvailable());
        bookRepository.save(book);
    }

    @Transactional
    // Ao atualizar ou deletar, remove o item do Redis para não servir dados obsoletos
    @CacheEvict(value = "books", key = "#id")
    public BookResponseDTO patchBook(Long id, PatchBookDTO dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com o ID: " + id));

        // Atualização parcial: altera apenas os campos que não forem null
        if (dto.title() != null) {
            book.setTitle(dto.title());
        }
        if (dto.isbn() != null) {
            book.setIsbn(dto.isbn());
        }
        if (dto.available() != null) {
            book.setAvailable(dto.available());
        }

        Book updatedBook = bookRepository.save(book);
        return toResponseDTO(updatedBook);
    }


    @Transactional 
    // Ao atualizar ou deletar, remove o item do Redis para não servir dados obsoletos
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livro não encontrado com o ID: " + id);
        }

        // Checa se o livro tem QUALQUER empréstimo cadastrado
    boolean hasLoans = loanRepository.existsByBookId(id);

    if (hasLoans) {
        throw new BusinessException("Não é possível excluir o livro porque ele possui empréstimos vinculados no sistema.");
    }

        bookRepository.deleteById(id);
    }

    // Método utilitário privado para evitar repetição do mapeamento Entity -> DTO
    private BookResponseDTO toResponseDTO(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getAuthor(),
                book.getTitle(),
                book.getIsbn(),
                book.getAvailable()
        );
    }
}