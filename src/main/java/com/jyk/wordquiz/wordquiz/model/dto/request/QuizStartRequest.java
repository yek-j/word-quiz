package com.jyk.wordquiz.wordquiz.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class QuizStartRequest {
    @NotNull
    private Long quizId;
    @NotNull
    private Long quizTypeId;
}
