package com.jyk.wordquiz.wordquiz.model.dto.request;

import com.jyk.wordquiz.wordquiz.common.type.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserRoleRequest {
    @NotNull
    private UserRole userRole;
}
