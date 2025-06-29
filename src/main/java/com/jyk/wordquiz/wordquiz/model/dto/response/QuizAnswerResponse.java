package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerResponse {
    private Long wordId;
    private boolean isCorrect;
    private String correctAnswer;
}
