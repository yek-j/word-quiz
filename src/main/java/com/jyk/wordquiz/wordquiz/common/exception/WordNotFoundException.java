package com.jyk.wordquiz.wordquiz.common.exception;

import jakarta.persistence.EntityNotFoundException;

public class WordNotFoundException extends EntityNotFoundException {
    public WordNotFoundException(Long id) {
        super("단어를 찾을 수 없습니다. ID: " + id);
    }
}
