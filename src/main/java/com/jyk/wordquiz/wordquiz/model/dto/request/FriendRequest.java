package com.jyk.wordquiz.wordquiz.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequest {
    @NotBlank
    private String friendUserName;
}
