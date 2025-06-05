package com.jyk.wordquiz.wordquiz.model.dto.request;

import com.jyk.wordquiz.wordquiz.common.type.QuizType;
import lombok.Getter;

@Getter
public class QuizStartRequest {
    private Long quizId;
    private QuizType quizType;
}
