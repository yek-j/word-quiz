package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
    Optional<UserConnection> findByTargetUser(User targetUser);
}
