package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LearningOverview {
    private int totalWordsLearned; // 학습한 단어 수
    private int totalQuizAttempts; // 총 퀴즈 시도 수
}
