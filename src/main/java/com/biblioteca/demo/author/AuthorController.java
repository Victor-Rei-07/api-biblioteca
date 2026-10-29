package com.biblioteca.demo.author;

import org.springframework.http.HttpStatus;
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

import io.swagger.v3.oas.annotations.media.ArraySchema;

@RestController
@RequestMapping("/authors")
@Tag(name = "Authors", description = "Endpoints para gerenciamento de autores")
public class AuthorController {

    private final AuthorService authorService;


    AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }     

    //Permite customizar os controllers no swagger
    @Operation(
        summary = "Buscar autores", //Descreve a operação do método http get books/{id} no swagger
        description = "Retorna todos os autores do banco." // descreve o que o método http faz
    )
    @ApiStandardResponses
    // Mostra os possíveis resultados http
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", //define código de status
            description = "Autore(s) encontrado(s) com sucesso", // define a descrição da resposta
            content = @Content(mediaType = "application/json", // mostra qual é o payload (schema) de resposta esperado 
            array = @ArraySchema(schema = @Schema(implementation = AuthorResponseDTO.class))) )
    })
    @GetMapping
    public ResponseEntity<Page<AuthorResponseDTO>> getAllAuthors(
        @RequestParam(required = false) String search,
        @ParameterObject @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable
    ) {
        Page<AuthorResponseDTO> result = authorService.findAllPaged(search, pageable);
        return ResponseEntity.ok(result);
    }

    
    
    
    //Permite customizar os controllers no swagger
    @Operation(
        summary = "Buscar autor por ID", //Descreve a operação do método http get books/{id} no swagger
        description = "Retorna os detalhes de um autor específico com base no ID fornecido." // descreve o que o método http faz
    )
    @ApiStandardResponses
    // Mostra os possíveis resultados http
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", //define código de status
            description = "Autor encontrado com sucesso.", // define a descrição da resposta
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorResponseDTO.class)) // mostra qual é o payload (schema) de resposta esperado 
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Autor não encontrado.",
            content = @Content // Oculta o schema da resposta caso seja vazio ou genérico
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> getById(@PathVariable Long id) {
     AuthorResponseDTO response = authorService.findById(id);
     return   ResponseEntity.ok(response); //faz o mesmo que ResponseEntity.status(HttpStatus.OK).body(response);
    }

    
    @Operation(
        summary = "Criar um autor",
        description = "Registra um novo autor no banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Autor criado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<AuthorResponseDTO> createAuthor(@RequestBody @Valid CreateAuthorDTO dto) {
        // O JSON já passou pelo DTO e foi validado!
        // Agora chamamos o Service para converter e salvar.
        AuthorResponseDTO response = authorService.createAuthor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    @Operation(
        summary = "Alterar um autor",
        description = "Executa uma modificação de um autor no banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Autor alterado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorResponseDTO.class))
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
    @PatchMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> patchAuthor(@PathVariable Long id, @RequestBody @Valid PatchAuthorDTO dto) {
        AuthorResponseDTO response = authorService.patchAuthor(id,dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    
    @Operation(
        summary = "Deletar um autor",
        description = "Exclui um autor do banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Autor excluído com sucesso",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Autor possui empréstimos vinculados",
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
