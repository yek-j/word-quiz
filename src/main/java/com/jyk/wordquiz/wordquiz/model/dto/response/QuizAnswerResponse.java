package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerResponse {
    private Long wordId;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private String correctAnswer;
}
