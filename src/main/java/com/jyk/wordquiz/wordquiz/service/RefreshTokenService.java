package com.jyk.wordquiz.wordquiz.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {
    private static final String REFRESH_TOKEN_KEY = "refresh-token-";
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void refreshTokenSave(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_KEY + userId.toString(),
                refreshToken,
                REFRESH_TOKEN_EXPIRE_DAYS,
                TimeUnit.DAYS
        );
    }

    public String findRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + userId.toString());
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_KEY + userId.toString());
    }
}
