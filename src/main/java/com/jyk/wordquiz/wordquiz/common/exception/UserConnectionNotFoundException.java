package com.jyk.wordquiz.wordquiz.common.exception;

public class UserConnectionNotFoundException extends RuntimeException {
    public UserConnectionNotFoundException(Long targetId, Long userId) {
        super(String.format("UserConnection을 찾을 수 없습니다. targetId: %d, userId: %d", targetId, userId));
    }
}
