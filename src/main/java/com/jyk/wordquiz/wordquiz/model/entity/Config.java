package com.jyk.wordquiz.wordquiz.model.entity;

import com.jyk.wordquiz.wordquiz.model.dto.request.ConfigRequest;
import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Entity
@Table(name = "config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_quiz_count", nullable = false)
    @Max(100)
    private int maxQuizCount;  // 사용자가 만들 수 있는 퀴즈 수

    @Column(name = "max_wordbook_count", nullable = false)
    @Max(100)
    private int maxWordBookCount;  // 사용자가 만들 수 있는 단어장 수

    @Column(name = "max_wordbooks_per_quiz", nullable = false)
    @Max(100)
    private int maxWordBooksPerQuiz;  // 퀴즈 안의 포함 가능한 단어장 수

    @Column(name = "max_words_per_book", nullable = false)
    @Max(100)
    private int maxWordsPerBook;  // 단어장 안의 단어 수

    public void update(ConfigRequest request) {
        this.maxQuizCount = request.getMaxQuizCount();
        this.maxWordBookCount = request.getMaxWordBookCount();
        this.maxWordBooksPerQuiz = request.getMaxWordBooksPerQuiz();
        this.maxWordsPerBook = request.getMaxWordsPerBook();
    }
}
