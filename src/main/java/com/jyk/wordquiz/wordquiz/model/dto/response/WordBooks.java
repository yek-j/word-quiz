package com.jyk.wordquiz.wordquiz.model.dto.response;

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
}
