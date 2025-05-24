package com.jyk.wordquiz.wordquiz.common.exception;

import lombok.Getter;

@Getter
public class AuthenticatedUserNotFoundException extends RuntimeException {
    private final Long userId;

    public AuthenticatedUserNotFoundException(Long userId) {
        super(String.format("인증된 사용자를 찾을 수 없습니다. userId: %d", userId));
        this.userId = userId;
    }

}
