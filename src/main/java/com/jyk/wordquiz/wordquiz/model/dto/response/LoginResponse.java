package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String username;
    private String email;
    private String token;
}
