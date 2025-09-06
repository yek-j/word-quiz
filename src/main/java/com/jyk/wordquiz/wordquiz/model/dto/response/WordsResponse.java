package com.jyk.wordquiz.wordquiz.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "단어 목록 응답")
public class WordsResponse {
    @Schema(description = "단어 목록")
    private List<Words> words = new ArrayList<>();

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPage;
}
