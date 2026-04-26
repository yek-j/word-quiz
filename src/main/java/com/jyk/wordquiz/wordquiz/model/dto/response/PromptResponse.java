package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.jyk.wordquiz.wordquiz.model.entity.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PromptResponse {
    private Long promptId;
    private String promptName;
    private String content;
    private Long promptTypeId;
    private String promptTypeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdUserName;
    private String lastModifiedUserName;
}
