package com.biblioteca.demo.book;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
public class BookSpecifications {
    public static Specification<Book> withFilter(String search, Boolean available) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro de Texto (titulo)
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                
                // Realiza o JOIN com a entidade Author (atributos: "author" e autor.get("name"))
                Join<Book, Object> authorJoin = root.join("author", JoinType.LEFT);

                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate authorLike = cb.like(cb.lower(authorJoin.get("name")), pattern);
    
                // Agrupa com OR: (lower(title) LIKE %search% OR lower(author.name) LIKE %search%)
                predicates.add(cb.or(titleLike, authorLike));
            }

                // 2. Filtro de Available (Available de forma exata)
            if (available != null ) {
                predicates.add(cb.equal(root.get("available"), available));
            }

            // Garante o DISTINCT na query para não duplicar linhas quando houver joins
            if (query != null) {
                query.distinct(true);
            }

            // Junta tudo com AND: (FiltroTexto) AND (FiltroAvailable)
            return cb.and(predicates.toArray(new Predicate[0]));
    };

    }
}
