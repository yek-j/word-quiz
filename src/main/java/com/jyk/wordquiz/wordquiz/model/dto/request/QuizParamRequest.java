package com.jyk.wordquiz.wordquiz.model.dto.request;

import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizParamRequest {
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String wordBookIds;
    @NotBlank
    private SharingStatus sharingStatus;
    @NotNull
    private List<Long> quizTypeIds; // 퀴즈타입 지정
}
