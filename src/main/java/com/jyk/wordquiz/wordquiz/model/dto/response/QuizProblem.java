package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizProblem {
    private String problem;
    private String answer;
    private boolean result;
}
