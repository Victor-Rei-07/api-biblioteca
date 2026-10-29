package com.biblioteca.demo.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.biblioteca.demo.config.ResponseTimeFilter;

import org.springframework.beans.factory.annotation.Value;

@Configuration //Diz que é uma classe de segurança
@EnableWebSecurity // Diz que essa classe pode configurar a segurança da web
@EnableMethodSecurity // Habilita o uso do @PreAuthorize
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;
    private final ResponseTimeFilter responseTimeFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityConfigurations(SecurityFilter securityFilter, CustomAuthenticationEntryPoint customAuthenticationEntryPoint, ResponseTimeFilter responseTimeFilter) {
        this.securityFilter = securityFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.responseTimeFilter = responseTimeFilter;
    }

    @Bean //Esse método cria a esteira
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Define a polítca como stateless
                // DESATIVA a injeção do 'no-store' padrão do Spring Security
                .headers(headers -> headers.cacheControl(cache -> cache.disable()))
                .authorizeHttpRequests(authorize -> authorize

                
                // 1. Rotas públicas de autenticação
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/config/custom-swagger.css"
                ).permitAll()
 
            // 2. Rotas restritas para ADMIN (ex: criar, editar ou deletar livros)
            .requestMatchers(HttpMethod.POST, "/books/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/books/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/books/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/books/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/authors/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/authors/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/authors/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/authors/**").hasRole("ADMIN")
        
            
 

    // 3. Rotas acessíveis por qualquer usuário autenticado (USER ou ADMIN)
             .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("USER", "ADMIN")
             .requestMatchers(HttpMethod.GET, "/authors/**").hasAnyRole("USER", "ADMIN")

    // 4. Qualquer outra requisição precisa estar apenas autenticada
    .anyRequest().authenticated()
                ).exceptionHandling(exception -> exception
            .authenticationEntryPoint(customAuthenticationEntryPoint) //Caso dê erro 401, redirecione para a classe customAuthenticationEntryPoint
        )
            .addFilterBefore(responseTimeFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class) //Aplica o filtro de segurança ANTES da autenticação
            .build();
    } 

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Injeta os valores da chave cors.allowed-origins como uma Lista de Strings
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Define as origens vindas do application.properties
        configuration.setAllowedOriginPatterns(allowedOrigins);
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD","PATCH"));
        
        // Headers permitidos na requisição (ex: Authorization para o JWT)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept","If-None-Match")); 
        
        // ATENÇÃO AQUI: Permite que o navegador/Swagger leia os cabeçalhos customizados
        configuration.setExposedHeaders(List.of("X-Response-Time", "Server-Timing","ETag"));    
        
        // Permite o envio de cookies ou credenciais, se necessário
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica essa configuração para todas as rotas da API
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}