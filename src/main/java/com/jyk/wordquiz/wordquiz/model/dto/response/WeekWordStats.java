package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WeekWordStats {
    private Long wordId;  
    private String term;  // 단어
    private String wordBookName; // 단어장 이름
    private int totalAttempts;  // 총 시도 횟수
    private int wrongAttempts;  // 틀린 횟수 
    private int correctAttemptts;  // 맞힌 횟수
    private double accuracyRate;  // 정답률
    private LocalDateTime lastAttempted; // 마지막 시도일
}
