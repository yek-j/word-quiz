package com.jyk.wordquiz.wordquiz.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class WordBookRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
}
