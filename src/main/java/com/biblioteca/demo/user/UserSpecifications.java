package com.biblioteca.demo.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.biblioteca.demo.domain.user.UserRole;

import jakarta.persistence.criteria.Predicate;

public class UserSpecifications {
    public static Specification<User> withFilter(String search, UserRole role) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro de Texto (Nome OU E-mail)
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
                
                // Agrupa com OR: (lower(name) LIKE %search% OR lower(email) LIKE %search%)
                predicates.add(cb.or(nameLike, emailLike));
            }

            // 2. Filtro de Role (Role de forma exata)
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            // Junta tudo com AND: (FiltroTexto) AND (FiltroRole)
            return cb.and(predicates.toArray(new Predicate[0]));
    };

    }
}
