package com.jyk.wordquiz.wordquiz.model.dto.response;

public record PromptValidateResponse(
        boolean valid,
        String message,
        AiQuizListResponse sample
) {}
