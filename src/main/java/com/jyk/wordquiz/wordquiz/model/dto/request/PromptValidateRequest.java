package com.jyk.wordquiz.wordquiz.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@Schema(description = "프롬프트 검증 요청")
public class PromptValidateRequest {

    @Pattern(
            regexp = "(?s).*\\{words}..*",
            message = "프롬프트에는 {words} 자리표시자가 포함되어야 합니다."
    )
    @Schema(description = "검증할 프롬프트 내용 (자리표시자 {words} 포함 필요)",
            example = "다음 단어 목록으로 빈칸 채우기 문제를 만들어주세요.\n{words}\n각 항목마다 wordId, sentence(빈칸은 ___), translation을 JSON 배열로 반환하세요.")
    @NotBlank(message = "프롬프트 내용은 필수입니다.")
    private String content;
}
