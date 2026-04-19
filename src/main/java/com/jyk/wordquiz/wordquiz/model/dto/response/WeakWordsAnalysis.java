package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WeakWordsAnalysis {
    private List<WeakWordStats> weakWords;
    private int totalAnalyzedWords;  // 총 단어 수
}
