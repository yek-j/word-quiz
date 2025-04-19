package com.jyk.wordquiz.wordquiz.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicationWordException extends RuntimeException {
    public DuplicationWordException(String message) {
        super(message);
    }

    public DuplicationWordException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicationWordException(String term, String description) {
        super(String.format("이미 존재하는 단어입니다. 단어: '%s', 설명: '%s'", term, description));
    }
}
