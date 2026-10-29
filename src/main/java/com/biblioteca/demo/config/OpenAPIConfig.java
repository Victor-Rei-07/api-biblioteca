package com.biblioteca.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    //Classe de configuração do Swagger
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Biblioteca")
                        .version("1.0")
                        .description("Documentação dos endpoints da API de empréstimos de livros"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth")) //Adicione o campo bearerAuth
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer") // Qual é o prefixo
                                        .bearerFormat("JWT"))); //Qual o formato do Bearer
    }

    @Bean
    public GlobalOpenApiCustomizer globalHeaderCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> { //Varra todos os paths, e pra cada um faça o seguinte
            ApiResponses apiResponses = operation.getResponses(); //Pegue todas as respostas da API

            //Adicionando as respostas
            apiResponses.addApiResponse("500", new ApiResponse().description("Erro interno do servidor"));
        }));
    }
}