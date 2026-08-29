package com.jyk.wordquiz.wordquiz.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateWordRequest {
    @NotBlank
    private String term;
    @NotBlank
    private String description;
    @NotNull
    private Long wordBookId;
}
