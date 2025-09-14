package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.jyk.wordquiz.wordquiz.model.entity.QuizWordBook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class WordBooks {
    private Long id;
    private String name;
    private String description;
    private String userName;
    private Long userId;
    private LocalDateTime createdAt;

    public WordBooks(QuizWordBook quizWordBook) {
        this.id = quizWordBook.getWordBook().getId();
        this.name = quizWordBook.getWordBook().getName();
        this.description = quizWordBook.getWordBook().getDescription();
        this.userName = quizWordBook.getWordBook().getCreatedBy().getUsername();
        this.userId = quizWordBook.getWordBook().getCreatedBy().getId();
        this.createdAt = quizWordBook.getWordBook().getCreatedAt();
    }
}
