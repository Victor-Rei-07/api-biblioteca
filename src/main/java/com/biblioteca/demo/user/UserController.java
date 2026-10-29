package com.biblioteca.demo.user;
 
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.demo.config.ApiStandardResponses;
import com.biblioteca.demo.domain.user.UserRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints para gerenciamento de usuários")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    
   
   

    //Permite customizar os controllers no swagger
    @Operation(
        summary = "Buscar usuários", //Descreve a operação do método http get books/{id} no swagger
        description = "Retorna todos os usuários do banco." // descreve o que o método http faz
    )
    @ApiStandardResponses
    // Mostra os possíveis resultados http
   @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", //define código de status
            description = "Usuário(s) encontrado(s) com sucesso", // define a descrição da resposta
            content = @Content(mediaType = "application/json", // mostra qual é o payload (schema) de resposta esperado 
            array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))),
        @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content 
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable
    ) {
        Page<UserResponseDTO> result = userService.findAllPaged(search, role, pageable);
        return ResponseEntity.ok(result);
    }
  

    
    @Operation(
        summary = "Buscar usuário por ID", //Descreve a operação do método http get books/{id} no swagger
        description = "Retorna os detalhes de um usuário específico com base no ID fornecido." // descreve o que o método http faz
    )
    @ApiStandardResponses
    // Mostra os possíveis resultados http
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", //define código de status
            description = "Usuário encontrado com sucesso.", // define a descrição da resposta
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class)) // mostra qual é o payload (schema) de resposta esperado 
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuário não encontrado.",
            content = @Content // Oculta o schema da resposta caso seja vazio ou genérico
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
     UserResponseDTO response = userService.findById(id);
     return   ResponseEntity.ok(response); //faz o mesmo que ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "Criar um usuário",
        description = "Registra um novo usuário no banco de dados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Usuário criado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        )
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid CreateUserDTO dto) {
        // O JSON já passou pelo DTO e foi validado!
        // Agora chamamos o Service para converter e salvar.
        UserResponseDTO response = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Alterar um usuário",
        description = "Executa uma modificação de um usuário no banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuário alterado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Campos inválidos",
            content = @Content 
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Usuário informado não encontrado",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @PatchMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<UpdateEmailUserResponseDTO> patchUser(@PathVariable Long id, @RequestBody @Valid PatchUserDTO dto) {
        // Se o #id da URL for diferente do id do usuário logado (e não for ADMIN), 
        // o Spring nem deixa entrar neste método e lança AccessDeniedException automaticamente!
        UpdateEmailUserResponseDTO response = userService.patchUser(id,dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "Deletar um usuário",
        description = "Exclui um usuário do banco de dados."
    )
    @ApiStandardResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Usuário excluído com sucesso",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Usuário possui empréstimos vinculados",
            content = @Content
        ),
         @ApiResponse(
            responseCode = "404", 
            description = "Usuário informado não encontrado", 
            content = @Content
        ),
         @ApiResponse(
            responseCode = "403", //define código de status
            description = "Acesso proibido - usuário sem a permissão necessária", // define a descrição da resposta
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
public ResponseEntity<UserResponseDTO> getMyProfile(Authentication authentication) {
    String email = authentication.getName();
    UserResponseDTO user = userService.findByEmailAuthenticated(email); 
    return ResponseEntity.ok(user); 
}
}
