package com.jyk.wordquiz.wordquiz.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerRequest {
    private Long wordId;
    private String answer;
}
