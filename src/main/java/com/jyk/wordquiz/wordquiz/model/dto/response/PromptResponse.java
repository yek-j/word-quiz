package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.jyk.wordquiz.wordquiz.common.type.PromptType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PromptResponse {
    private Long promptId;
    private String promptName;
    private String content;
    private PromptType promptType;
    private boolean disabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdUserName;
    private String lastModifiedUserName;
}
