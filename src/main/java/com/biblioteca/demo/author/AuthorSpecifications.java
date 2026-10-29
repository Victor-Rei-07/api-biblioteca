package com.biblioteca.demo.author;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
public class AuthorSpecifications {
    public static Specification<Author> withFilter(String search) {
        return (root, query, cb) -> { //root representa a tabela; query -> tudo que é de pesquisa fora do WHERE ( DISTINCT/ORDER BY,GROU BY,COUNT etc); cb -> tudo após o WHERE
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro de Texto (titulo)
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%"; //pattern =  %LOWER(search)%
                
                Predicate authorLike = cb.like(cb.lower(root.get("name")), pattern); //WHERE LOWER(name) LIKE pattern
                     
                predicates.add(authorLike);
            }

            // Junta tudo com AND: (FiltroTexto) AND (FiltroAvailable)
            return cb.and(predicates.toArray(new Predicate[0]));
    };

    }
}
