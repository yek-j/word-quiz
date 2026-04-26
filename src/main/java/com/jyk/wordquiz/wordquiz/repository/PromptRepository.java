package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    @Query("SELECT p FROM Prompt p WHERE p.promptName LIKE %:promptName% AND (:quizTypeName IS NULL OR p.promptType.quizTypeName LIKE %:quizTypeName%)")
    Page<Prompt> findByPromptNameAndType(@Param("promptName") String promptName, @Param("quizTypeName") String quizTypeName, Pageable pageable);

    boolean existsByPromptType_Id(Long promptTypeId);

    boolean existsByPromptType_IdAndIdNot(Long promptTypeId, Long id);

    void deleteByPromptType_Id(Long promptTypeId);

    Optional<Prompt> findByPromptTypeId(Long promptTypeId);
}