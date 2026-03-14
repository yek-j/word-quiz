package com.jyk.wordquiz.wordquiz.model.dto.request;

import com.jyk.wordquiz.wordquiz.common.type.PromptType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "프롬프트 추가 요청")
public class PromptRequest {

    @Schema(description = "프롬프트 타입", example = "AI_FILL_IN_BLANK")
    @NotNull(message = "프롬프트 타입은 필수입니다.")
    private PromptType promptType;

    @Schema(description = "프롬프트 이름", example = "빈칸 문제 기본 프롬프트")
    @NotBlank(message = "프롬프트 이름은 필수입니다.")
    private String promptName;

    @Schema(description = "프롬프트 내용", example = "다음 단어로 빈칸 문제를 만들어주세요.")
    @NotBlank(message = "프롬프트 내용은 필수입니다.")
    private String content;
}