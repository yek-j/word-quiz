package com.jyk.wordquiz.wordquiz.common.exception;

import jakarta.persistence.EntityNotFoundException;

public class QuizSessionNotFoundException extends EntityNotFoundException {
    public QuizSessionNotFoundException(Long id) {
        super("퀴즈 세션을 찾을 수 없습니다. ID: " + id);
    }
}
