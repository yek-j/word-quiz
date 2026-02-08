package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.common.type.UserConnectionStatus;
import com.jyk.wordquiz.wordquiz.common.type.UserConnectionType;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.UserConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
    Optional<UserConnection> findByUserAndTargetUser(User user, User targetUser);
    Page<UserConnection> findByTargetUserAndConnectionStatus(User targetUser, UserConnectionStatus connectionStatus, Pageable pageable);
    Page<UserConnection> findByUserAndConnectionTypeAndConnectionStatus(User user, UserConnectionType connectionType, UserConnectionStatus connectionStatus, Pageable pageable);
    Optional<UserConnection> findByUserAndTargetUserAndConnectionTypeAndConnectionStatus(User user, User targetUser, UserConnectionType userConnectionType, UserConnectionStatus userConnectionStatus);

}
