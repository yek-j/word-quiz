package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.QuizType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizTypeRepository extends JpaRepository<QuizType, Long> {
    Page<QuizType> findByQuizTypeNameContaining(String quizTypeName, Pageable pageable);

    List<QuizType> findByIdIn(List<Long> ids);

    @Query("SELECT q FROM QuizType q WHERE q.useAi = false " +
            "OR (q.useAi = true AND EXISTS (SELECT 1 FROM Prompt p WHERE p.promptType = q))")
    List<QuizType> findAvailableForQuiz();
}
