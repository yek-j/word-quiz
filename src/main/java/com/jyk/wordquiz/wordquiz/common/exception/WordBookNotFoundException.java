package com.jyk.wordquiz.wordquiz.common.exception;

import jakarta.persistence.EntityNotFoundException;

public class WordBookNotFoundException extends EntityNotFoundException {
  public WordBookNotFoundException(Long id) {
    super("단어장을 찾을 수 없습니다. ID: " + id);
  }
}
