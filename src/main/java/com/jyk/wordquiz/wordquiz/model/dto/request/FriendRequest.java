package com.jyk.wordquiz.wordquiz.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequest {
    @NotBlank
    @Size(max=50)
    private String friendUserName;
}
