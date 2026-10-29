package com.biblioteca.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class ResponseTimeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
                                    throws ServletException, IOException {
        
        // 1. Envolve a resposta num wrapper que segura os dados na memória RAM
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);  
        long startTime = System.currentTimeMillis(); //inicia o tempo

        try {
            // 2. Passa a resposta ENVOLVIDA (responseWrapper) para os próximos filtros e Controllers
            filterChain.doFilter(request, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime; //calcula a diferença

            // 3. Adiciona os cabeçalhos antes de liberar o fluxo para o cliente
            responseWrapper.setHeader("X-Response-Time", duration + " ms");
            responseWrapper.setHeader("Server-Timing", "app;dur=" + duration + ";desc=\"Execution Time\"");

            // 4. OBRIGATÓRIO: Copia o corpo do buffer para a resposta real que vai para o cliente!
            responseWrapper.copyBodyToResponse();
        }
    }
}