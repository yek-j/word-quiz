package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.model.entity.LoginLog;
import com.jyk.wordquiz.wordquiz.repository.LoginLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginLogService {
    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Long userId, String email, String userAgent, String ip, boolean success) {
        loginLogRepository.save(LoginLog.builder()
                .userId(userId)
                .attemptedEmail(email)
                .userAgent(userAgent)
                .userClientIp(ip)
                .isSuccess(success)
                .build());
    }
}
