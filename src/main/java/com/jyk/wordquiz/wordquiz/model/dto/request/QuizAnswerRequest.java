package com.jyk.wordquiz.wordquiz.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerRequest {
    @NotNull
    private Long wordId;
    private String answer;
}
