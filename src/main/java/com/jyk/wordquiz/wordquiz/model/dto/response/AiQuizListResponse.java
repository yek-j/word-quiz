package com.jyk.wordquiz.wordquiz.model.dto.response;

import java.util.List;

public record AiQuizListResponse(List<Item> problems) {
    public record Item(Long wordId, String sentence, String translation) {}
}
