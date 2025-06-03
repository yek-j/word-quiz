package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import com.jyk.wordquiz.wordquiz.model.entity.Quiz;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByCreatedBy(User user, Pageable pageable);

    Optional<Quiz> findByCreatedByAndId(User user, Long id);

    @Query("SELECT q FROM Quiz q WHERE q.createdBy = :user OR (q.sharingStatus = :status AND q.createdBy != :user)")
    Page<Quiz> findBySharingStatusOrMy(@Param("status") SharingStatus status, @Param("user") User user, Pageable pageable);
}
