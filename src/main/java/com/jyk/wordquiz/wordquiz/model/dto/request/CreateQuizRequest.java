package com.jyk.wordquiz.wordquiz.model.dto.request;

import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuizRequest {
    private String name;
    private String description;
    private String wordBookIds;
    private SharingStatus sharingStatus;
}
