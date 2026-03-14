package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.common.type.PromptType;
import com.jyk.wordquiz.wordquiz.model.entity.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    /**
     * Find prompts whose name contains the given substring and, if provided, match the specified type, returned as a pageable result.
     *
     * @param promptName substring to match within each Prompt's name (search uses contains semantics)
     * @param promptType optional PromptType to filter results; if null, no type filtering is applied
     * @param pageable pagination and sorting information for the result set
     * @return a Page of Prompt entities matching the name substring and optional type filter
     */
    @Query("SELECT p FROM Prompt p WHERE p.promptName LIKE %:promptName% AND (:promptType IS NULL OR p.promptType = :promptType)")
    Page<Prompt> findByPromptNameAndType(@Param("promptName") String promptName, @Param("promptType") PromptType promptType, Pageable pageable);
}