package com.jyk.wordquiz.wordquiz.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateUserException extends RuntimeException{
    public DuplicateUserException(String message) {
        super(message);
    }

    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }

    // 일반적으로 사용되는 메시지로 예외를 생성하는 생성자
    public DuplicateUserException(String username, String email) {
        super(String.format("이미 존재하는 사용자입니다. 사용자명: '%s', 이메일: '%s'", username, email));
    }
}
