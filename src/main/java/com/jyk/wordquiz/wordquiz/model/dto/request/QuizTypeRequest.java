package com.jyk.wordquiz.wordquiz.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizTypeRequest {
    @NotBlank
    private String quizTypeName;
    private String quizTypeDescription;
    @NotNull
    private boolean useAi;
}
