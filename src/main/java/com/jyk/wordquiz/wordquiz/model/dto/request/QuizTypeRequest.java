package com.jyk.wordquiz.wordquiz.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizTypeRequest {
    private String quizTypeName;
    private String quizTypeDescription;
    private boolean useAi;
}
