package com.jyk.wordquiz.wordquiz.model.dto.request;

import lombok.Getter;

@Getter
public class UpdateWordRequest {
    private String term;
    private String description;
    private Long wordBookId;
}
