package com.jyk.wordquiz.wordquiz.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChangePwd {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
}
