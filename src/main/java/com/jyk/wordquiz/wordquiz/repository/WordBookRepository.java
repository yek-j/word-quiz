package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface WordBookRepository extends JpaRepository<WordBook, Long> {
    Page<WordBook> findByCreatedBy(User createdBy, Pageable pageable);
    Optional<WordBook> findByIdAndCreatedBy(Long id, User createdBy);
}
