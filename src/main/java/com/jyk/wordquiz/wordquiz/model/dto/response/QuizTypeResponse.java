package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuizTypeResponse {
    private Long quizTypeId;
    private String quizTypeName;
    private String quizTypeDescription;
    private boolean useAi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdUserName;
    private String lastModifiedUserName;
}