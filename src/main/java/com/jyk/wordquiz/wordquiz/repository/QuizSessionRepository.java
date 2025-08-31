package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.Quiz;
import com.jyk.wordquiz.wordquiz.model.entity.QuizSession;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    @Query(value = "SELECT qs.* FROM quiz_session qs " +
            "WHERE qs.user_id = :uid " +
            "AND qs.quiz_id IN :qids " +
            "AND qs.attempted_at = (" +
            "    SELECT MAX(qs2.attempted_at) " +
            "    FROM quiz_session qs2 " +
            "    WHERE qs2.user_id = :uid " +
            "    AND qs2.quiz_id = qs.quiz_id" +
            ")",
            nativeQuery = true)
    List<QuizSession> findLatestSessionsByUserAndQuizIds(@Param("uid") Long userId,
                                                         @Param("qids") List<Long> quizIds);

    Optional<QuizSession> findByUserAndQuizAndIsQuizActive(User user, Quiz quiz, boolean active);
    Optional<QuizSession> findByIdAndUser(Long id, User user);

    int countByUser(User user);

    @Query(value = "SELECT COUNT(DISTINCT qq.word_id) " +
            "FROM quiz_session qs " +
            "JOIN quiz_question qq ON qs.id = qq.quiz_session_id " +
            "WHERE qs.user_id = :userId",
            nativeQuery = true)
    int countDistinctWordsByUser(@Param("userId") Long userId);

    List<QuizSession> findByUserAndIsQuizActive(User user, boolean isActive);
    List<QuizSession> findByUser(User user);
}
