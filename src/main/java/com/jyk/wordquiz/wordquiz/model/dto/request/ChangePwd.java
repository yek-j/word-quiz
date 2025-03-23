package com.jyk.wordquiz.wordquiz.model.dto.request;

import lombok.Getter;

@Getter
public class ChangePwd {
    private String currentPassword;
    private String newPassword;
}
