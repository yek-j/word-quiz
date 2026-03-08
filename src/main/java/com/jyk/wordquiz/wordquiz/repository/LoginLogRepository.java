package com.jyk.wordquiz.wordquiz.repository;

import com.jyk.wordquiz.wordquiz.model.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    @Query("SELECT l.userId, MAX(l.loginAt) FROM LoginLog l WHERE l.userId IN(:userIds) GROUP BY l.userId")
    List<Object[]> findUserLastLoginAt(@Param("userIds") List<Long> userIds);
}
