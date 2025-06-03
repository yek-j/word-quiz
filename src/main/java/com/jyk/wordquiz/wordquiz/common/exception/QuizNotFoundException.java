package com.jyk.wordquiz.wordquiz.common.exception;

import jakarta.persistence.EntityNotFoundException;

public class QuizNotFoundException extends EntityNotFoundException {
    public QuizNotFoundException(Long id) {
        super("퀴즈를 찾을 수 없습니다. ID: " + id);
    }
}
