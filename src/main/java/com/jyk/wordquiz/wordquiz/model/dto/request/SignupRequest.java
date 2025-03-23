package com.jyk.wordquiz.wordquiz.model.dto.request;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String email;
    private String password;
}
