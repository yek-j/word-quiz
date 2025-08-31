package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class QuizStats {
    private Long quizId;
    private String quizName;
    private int attemptCount;
    private int bestScore;
    private double averageScore;
    private int lastScore;
    private LocalDateTime lastAttempted;
}
