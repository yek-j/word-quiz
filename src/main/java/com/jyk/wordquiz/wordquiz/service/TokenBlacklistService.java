package com.jyk.wordquiz.wordquiz.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 로그아웃된 Access Token을 만료 시점까지 무효 처리하기 위한 블랙리스트.
 * 키: "access-blacklist-{jti}" / 값: "1" / TTL: 토큰 남은 만료시간(ms)
 */
@Service
public class TokenBlacklistService {
    private static final String BLACKLIST_KEY_PREFIX = "access-blacklist-";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String jti, long ttlMillis) {
        if (jti == null || ttlMillis <= 0) return;
        redisTemplate.opsForValue().set(
                BLACKLIST_KEY_PREFIX + jti,
                "1",
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null) return false;
        Boolean exists = redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti);
        return Boolean.TRUE.equals(exists);
    }
}
