package com.biblioteca.demo.book;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.demo.config.ApiStandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;


@RestController
@RequestMapping("/books")
@Tag(name = "Books", description = "Endpoints para gerenciamento de livros")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    //Permite customizar os controllers no swagger
    @Operation(
        summary = "Buscar livros", //Descreve a operação do método http get books/{id} no swagger
        description = "Retorna todos os livros do banco." // descreve o que o método http faz
    )
    @ApiStandardResponses
    // Mostra os possíveis resultados http
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", //define código de status
            description = "Livro(s) encontrado(s) com sucesso", // define a descrição da resposta
            content = @Content(mediaType = "application/json", // Usa @ArraySchema para indicar que é uma lista de BookResponseDTO
            array = @ArraySchema(schema = @Schema(implementation = BookResponseDTO.class))) 
        )
    })
    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getAllBooks(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean available,
        @ParameterObject @PageableDefault(page = 0, size = 10, sort = "title") Pageable pageable
    )
     {
        Page<BookResponseDTO> result = bookService.findAllPaged(search, available, pageable);
        return ResponseEntity.ok(result);
    }

    //Permite customizar os controllers no swagger
    @Operation(
        summary = "Buscar livro por ID", //Descreve a operação do método http get books/{id} no swagger
        description = "Retorna os detalhes de um livro específico com base no ID fornecido."// descreve o que o método http faz 
    )
    @ApiStandardResponses
    // Mostra os possíveis resultados http
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", //define código de status
            description = "Livro encontrado com sucesso.", // define a descrição da resposta
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)) // mostra qual é o payload (schema) de resposta esperado 
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Livro não encontrado.",
            content = @Content // Oculta o schema da resposta caso seja vazio ou genérico
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @Operation(
        summary = "Criar um livro",
        description = "Registra um novo livro no banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Livro criado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Autor informado não encontrado",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@RequestBody @Valid CreateBookDTO dto) {
        BookResponseDTO response = bookService.createBook(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    

    @Operation(
        summary = "Alterar um livro",
        description = "Executa uma modificação de um livro no banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Livro alterado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Livro informado não encontrado",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<BookResponseDTO> patchBook(
            @PathVariable Long id, 
            @RequestBody @Valid PatchBookDTO dto
    ) {
        BookResponseDTO response = bookService.patchBook(id, dto);
        return ResponseEntity.ok(response);
    }

    
    @Operation(
        summary = "Deletar um livro",
        description = "Exclui um livro do banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Livro excluído com sucesso",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Livro possui empréstimos vinculados",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Livro informado não encontrado",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}