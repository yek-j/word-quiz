package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Quizzes {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private boolean isQuizActive = false;

    public Quizzes(Long id, String name, String description, String username, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = username;
        this.createdAt = createdAt;
    }
}
