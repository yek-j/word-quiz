package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
