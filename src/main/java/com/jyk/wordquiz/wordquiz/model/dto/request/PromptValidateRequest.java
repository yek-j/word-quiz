package com.jyk.wordquiz.wordquiz.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "프롬프트 검증 요청")
public class PromptValidateRequest {

    @Schema(description = "검증할 프롬프트 내용", example = "다음 단어로 빈칸 문제를 만들어주세요. 단어: {word}, 의미: {meaning}")
    @NotBlank(message = "프롬프트 내용은 필수입니다.")
    private String content;
}
