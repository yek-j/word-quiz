package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.TooManyLoginAttemptsException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 로그인 실패 횟수를 Redis로 세어 무차별 대입(brute force)을 차단한다.
 *
 * - 이메일 기준
 */
@Service
public class LoginAttemptService {

    private static final String KEY_PREFIX = "login-fail-";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public LoginAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 잠금 상태면 예외를 던진다. 비밀번호를 검증하기 전에 호출해야 한다.
     * (검증 뒤에 호출하면 잠긴 계정도 비밀번호 추측 시도를 계속 받게 된다)
     */
    public void checkBlocked(String email) {
        String key = KEY_PREFIX + email;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return;
        }

        if (Integer.parseInt(value) >= MAX_ATTEMPTS) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            long remain = (ttl != null && ttl > 0) ? ttl : 0;
            throw new TooManyLoginAttemptsException(
                    "로그인 시도 횟수를 초과했습니다. " + remain + "초 후 다시 시도해주세요.");
        }
    }

    /**
     * 로그인 실패 시 카운터를 1 올린다.
     *
     * TTL은 첫 실패(count == 1)일 때만 건다. 실패할 때마다 갱신하면 공격자가 계속 시도해
     * 영구 잠금을 만들 수 있다. 첫 실패로부터 LOCK_DURATION이 지나면 자동 해제되는 게 맞다.
     */
    public void recordFailure(String email) {
        String key = KEY_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            redisTemplate.expire(key, LOCK_DURATION);
        }
    }

    /** 로그인 성공 시 카운터를 지운다. */
    public void reset(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
