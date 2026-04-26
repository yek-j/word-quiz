package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.Quiz;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByCreatedByAndId(User user, Long id);

    /**
     * getQuizList 통합 검색
     * - kind 'MY'      : 사용자 본인 퀴즈
     * - kind 'FRIENDS' : 친구의 PUBLIC/FRIENDS 퀴즈
     * - kind 'ALL'     : 본인 + 다른 사람 PUBLIC + 친구 FRIENDS
     * - searchId 지정 시 해당 사용자 작성 퀴즈로 한정 (visibility 규칙은 그대로 적용)
     * - typeIds 지정 시 allowedTypes에 하나라도 포함된 퀴즈로 필터
     */
    @Query("SELECT q FROM Quiz q WHERE " +
            "(:searchId IS NULL OR q.createdBy.id = :searchId) " +
            "AND (" +
                "(:kind = 'MY' AND q.createdBy = :user) " +
                "OR (:kind = 'FRIENDS' AND q.createdBy IN (" +
                    "SELECT uc.targetUser FROM UserConnection uc " +
                    "WHERE uc.user = :user AND uc.connectionStatus = 'ACCEPTED'" +
                ") AND q.sharingStatus IN ('PUBLIC', 'FRIENDS')) " +
                "OR (:kind = 'ALL' AND (" +
                    "q.sharingStatus = 'PUBLIC' " +
                    "OR q.createdBy = :user " +
                    "OR (q.createdBy IN (" +
                        "SELECT uc.targetUser FROM UserConnection uc " +
                        "WHERE uc.user = :user AND uc.connectionStatus = 'ACCEPTED'" +
                    ") AND q.sharingStatus = 'FRIENDS')" +
                "))" +
            ") " +
            "AND (:typeIds IS NULL OR q.id IN (" +
                "SELECT q2.id FROM Quiz q2 JOIN q2.allowedTypes t WHERE t.id IN :typeIds" +
            "))"
    )
    Page<Quiz> searchQuizzes(@Param("user") User user,
                             @Param("kind") String kind,
                             @Param("searchId") Long searchId,
                             @Param("typeIds") List<Long> typeIds,
                             Pageable pageable);
}
