package com.jyk.wordquiz.wordquiz.repository;

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

    // 친구의 'PUBLIC', 'FRIENDS' 공개 상태를 모두 가져오기
    @Query("SELECT q FROM Quiz q WHERE " +
            "(q.createdBy IN(" +
            "SELECT uc.targetUser FROM UserConnection uc " +
            "WHERE uc.user = :user AND uc.connectionStatus = 'ACCEPTED'" +
            ") AND q.sharingStatus in ('PUBLIC', 'FRIENDS')) "
    )
    Page<Quiz> findByFriendQuizzes(@Param("user") User user,  Pageable pageable);

    // 사용자 본인의 PUBLIC, PRIVATE, FRIENDS
    // 타인의 PUBLIC, 친구의 FRIENDS인 퀴즈 가져오기
    @Query("SELECT q FROM Quiz q WHERE " +
            "q.sharingStatus = 'PUBLIC' " +
            "OR q.createdBy = :user " +
            "OR (q.createdBy IN(" +
                "SELECT uc.targetUser FROM UserConnection uc " +
                "WHERE uc.user = :user AND uc.connectionStatus = 'ACCEPTED'" +
            ") AND q.sharingStatus = 'FRIENDS')"
    )
    Page<Quiz> findAccessibleQuizzes(@Param("user") User user, Pageable pageable);

    // 특정 사용자 퀴즈 검색
    @Query("SELECT q FROM Quiz q WHERE " +
            "q.createdBy.id = :searchId " +
            "AND (q.sharingStatus = 'PUBLIC' " +
            "     OR (q.createdBy IN(" +
                "SELECT uc.targetUser FROM UserConnection uc " +
                "WHERE uc.user = :user AND uc.connectionStatus = 'ACCEPTED'" +
            ") AND q.sharingStatus = 'FRIENDS'))"
    )
    Page<Quiz> findSearchIdQuizzes(@Param("user") User user,
                                   @Param("searchId") Long searchId,
                                   Pageable pageable);
}
