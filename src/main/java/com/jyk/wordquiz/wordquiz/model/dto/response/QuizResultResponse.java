package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class QuizResultResponse {
    private int score;  // 정답 수
    private long totalQuestions; // 채점 완료한 문제 수
    private LocalDateTime completedAt; // 마지막 퀴즈 시도(종료 시간)
}
