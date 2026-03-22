package com.jyk.wordquiz.wordquiz.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigRequest {
    private int maxQuizCount;  // 사용자가 만들 수 있는 퀴즈 수
    private int maxWordBookCount;  // 사용자가 만들 수 있는 단어장 수
    private int maxWordBooksPerQuiz;  // 퀴즈 안의 포함 가능한 단어장 수
    private int maxWordsPerBook;  // 단어장 안의 단어 수
}
