package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.jyk.wordquiz.wordquiz.common.type.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminUsers {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private UserRole role;
}
