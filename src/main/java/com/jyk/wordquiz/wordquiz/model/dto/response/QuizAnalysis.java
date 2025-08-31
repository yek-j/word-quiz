package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizAnalysis {
    List<QuizStats> quizStats;
    private int totalAnalyzedQuizzes;
}
