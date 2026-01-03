package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Friend {
    private Long userId;
    private String username;
}
