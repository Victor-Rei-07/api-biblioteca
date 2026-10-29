package com.biblioteca.demo.author;


import java.util.List;

import org.springframework.stereotype.Service;

import com.biblioteca.demo.book.BookRepository;
import com.biblioteca.demo.infra.exception.*;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import jakarta.transaction.Transactional;



@Service
public class AuthorService  {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;


    AuthorService(AuthorRepository authorRepository,BookRepository bookRepository) {
        this.authorRepository = authorRepository;
         this.bookRepository = bookRepository;
    }


    public AuthorResponseDTO findById(Long id) {
    return authorRepository.findById(id) // Retorna um Optional<Author>
        .map(author -> new AuthorResponseDTO( // Se achar, converte pra DTO
            author.getId(),
            author.getName(),
            author.getBio()
        ))
        .orElseThrow(() -> new ResourceNotFoundException("Autor não encontrado com o ID: " + id));// Se estiver vazio, lança erro
    }

    @Transactional
    public void deleteAuthor(Long id) {
       if (!authorRepository.existsById(id)) {
        throw new ResourceNotFoundException("Autor não encontrado com o ID: " + id);
    }

    // Checa se o autor tem QUALQUER livro cadastrado
    boolean hasBooks = bookRepository.existsByAuthorId(id);

    if (hasBooks) {
        throw new BusinessException("Não é possível excluir o autor porque ele possui livros vinculados no catálogo.");
    }

    authorRepository.deleteById(id);
    }

    public AuthorResponseDTO createAuthor(CreateAuthorDTO dto) {
        // 1. Converte DTO -> Entity
        Author author = new Author();
        author.setName(dto.name());
        author.setBio(dto.bio());

        // 2. Salva a Entity no PostgreSQL
        Author savedAuthor = authorRepository.save(author);

        // 3. Converte a Entity salva -> DTO de Resposta
        return new AuthorResponseDTO(
            savedAuthor.getId(),
            savedAuthor.getName(),
            savedAuthor.getBio()
        );
    }

    public AuthorResponseDTO patchAuthor(Long id, PatchAuthorDTO dto) {
    Author author = authorRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Autor não encontrado com o ID: " + id));

    // Atualiza o nome APENAS se o cliente enviou um nome no JSON
    if (dto.name() != null) {
        author.setName(dto.name());
    }

    // Atualiza a bio APENAS se o cliente enviou uma bio no JSON
    if (dto.bio() != null) {
        author.setBio(dto.bio());
    }

    Author updatedAuthor = authorRepository.save(author);
    return toResponseDTO(updatedAuthor);
}
    public List<AuthorResponseDTO> findAll() {
        return authorRepository.findAll()
        .stream() // Cria uma "esteira" e bota os livros nela
        .map(this::toResponseDTO) //coloca cada item da esteira em um DTO
        .toList(); // retorna todos os DTOs
    }

    public Page<AuthorResponseDTO> findAllPaged(String search, Pageable pageable) {
       Specification<Author> spec = AuthorSpecifications.withFilter(search); //constrói o "WHERE"
        return authorRepository.findAll(spec, pageable).map(this::toResponseDTO);  
    }

    
    // Método utilitário privado para evitar repetição do mapeamento Entity -> DTO
    private AuthorResponseDTO toResponseDTO(Author author) {
        return new AuthorResponseDTO(
                author.getId(),
                author.getName(),
                author.getBio()
        );
    }
}
