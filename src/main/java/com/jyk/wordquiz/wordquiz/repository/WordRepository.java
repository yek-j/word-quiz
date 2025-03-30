package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {
}
