package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class QuizProblem {
    private Long wordId;
    private String problem;
    private String answer;
    private Boolean result;
}
