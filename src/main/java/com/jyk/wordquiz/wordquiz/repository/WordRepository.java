package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.Word;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
    Page<Word> findByWordBook(WordBook wordBook, Pageable pageable);
    Optional<Word> findByIdAndWordBook(Long id, WordBook wordBook);
    Optional<Word> findByTermAndWordBook(String term, WordBook wordBook);
}
