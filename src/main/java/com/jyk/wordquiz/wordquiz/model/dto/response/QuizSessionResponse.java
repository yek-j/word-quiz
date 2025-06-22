package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.jyk.wordquiz.wordquiz.common.type.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionResponse {
    private Long sessionId;
    private List<QuizProblem> quizProblems;
    private QuizType quizType;
}
