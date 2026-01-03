package com.jyk.wordquiz.wordquiz.common.exception;

import jakarta.persistence.EntityNotFoundException;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(Long userId) {
        super(String.format("사용자를 찾을 수 없습니다. userId: %d", userId));
    }
}
