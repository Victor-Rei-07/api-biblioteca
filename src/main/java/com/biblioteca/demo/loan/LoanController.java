package com.biblioteca.demo.loan;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.biblioteca.demo.config.ApiStandardResponses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/loans")
@Tag(name = "Loans", description = "Endpoints para gerenciamento de empréstimos")
public class LoanController {
   
    private final LoanService loanService;

    LoanController(LoanService loanService) {
        this.loanService = loanService;
    }



    //Permite customizar os controllers no swagger
    @Operation(
        summary = "Buscar empréstimos", //Descreve a operação do método http get books/{id} no swagger
        description = "Retorna todos os usuários do banco." // descreve o que o método http faz
    )
    @ApiStandardResponses
    // Mostra os possíveis resultados http
   @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", //define código de status
            description = "Empréstimos encontrado(s) com sucesso", // define a descrição da resposta
            content = @Content(mediaType = "application/json", // mostra qual é o payload (schema) de resposta esperado 
            array = @ArraySchema(schema = @Schema(implementation = LoanResponseDTO.class)))),
        @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content 
        )
    })
    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.findMyLoans());
    }

    
    
    @Operation(
        summary = "Buscar empréstimo por ID", 
        description = "Retorna os detalhes de um empréstimo específico com base no ID fornecido."
    )
    @ApiStandardResponses
    // Mostra os possíveis resultados http
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", //define código de status
            description = "Empréstimo encontrado com sucesso", // define a descrição da resposta
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class)) // mostra qual é o payload (schema) de resposta esperado 
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Empréstimo não encontrado",
            content = @Content // Oculta o schema da resposta caso seja vazio ou genérico
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> getById(@PathVariable Long id) {
     LoanResponseDTO response = loanService.findById(id);
     return   ResponseEntity.ok(response); //faz o mesmo que ResponseEntity.status(HttpStatus.OK).body(response);
    }

    
    @Operation(
        summary = "Criar um empréstimo",
        description = "Registra um novo empréstimo no banco de dados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Empréstimo criado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Usuário ou livro não encontrado",
            content = @Content 
        )
    })
    @PostMapping
    public ResponseEntity<LoanResponseDTO> createLoan(@RequestBody @Valid CreateLoanDTO dto) {
        // O JSON já passou pelo DTO e foi validado!
        // Agora chamamos o Service para converter e salvar.
        LoanResponseDTO response = loanService.createLoan(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Finalizar um empréstimo",
        description = "Termina um empréstimo sem excluir ele no banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Empréstimo finalizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Empréstimo informado não encontrado",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> endLoan(@PathVariable Long id) {
        LoanResponseDTO response = loanService.endLoan(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Alterar um empréstimo",
        description = "Executa uma modificação de um empréstimo no banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Empréstimo alterado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoanResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Empréstimo informado não encontrado",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> patchLoan (@PathVariable Long id, @RequestBody @Valid PatchLoanDTO dto) {
        LoanResponseDTO response = loanService.patchLoan(id,dto);
        return ResponseEntity.ok(response);
    }

    
    @Operation(
        summary = "Deletar um empréstimo",
        description = "Exclui um empréstimo do banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Empréstimo excluído com sucesso",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Empréstimo informado possui usuário ou livro vinculados",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Empréstimo informado não encontrado", 
            content = @Content
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.endLoan(id);
        return ResponseEntity.noContent().build();
    }
}
