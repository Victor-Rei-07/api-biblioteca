package com.biblioteca.demo.book;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book>  {

    Boolean existsByAuthorId(Long authorId);
    
    @Override
    @EntityGraph(attributePaths = "author")
    Page<Book> findAll(
        Specification<Book> spec,
        Pageable pageable
    );
}
