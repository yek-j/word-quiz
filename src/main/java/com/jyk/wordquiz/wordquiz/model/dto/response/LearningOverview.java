package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LearningOverview {
    private int totalWordsLearned; // 학습한 단어 수
    private int totalQuizAttempts; // 총 퀴즈 시도 수
    private int consecutiveStudyDays; // 연속 공부일
    private boolean studiedToday; // 오늘 공부했는지
    private int thisWeekQuizCount; // 이번 주 퀴즈 수
    private LocalDate lastStudyDate; // 마지막 공부일
}
