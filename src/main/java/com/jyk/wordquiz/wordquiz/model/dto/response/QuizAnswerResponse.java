package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerResponse {
    @NotBlank
    private Long wordId;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    @NotBlank
    private String correctAnswer;
}
