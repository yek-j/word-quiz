package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordBookRepository extends JpaRepository<WordBook, Long> {
}
