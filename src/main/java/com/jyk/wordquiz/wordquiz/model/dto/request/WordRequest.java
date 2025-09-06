package com.jyk.wordquiz.wordquiz.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "단어 추가 요청")
public class WordRequest {
    @Schema(description = "단어", example = "apple")
    @NotBlank(message = "단어는 필수입니다.")
    private String term;
    @Schema(description = "단어 뜻", example = "사과")
    @NotBlank(message = "단어 뜻은 필수입니다.")
    private String description;
}
