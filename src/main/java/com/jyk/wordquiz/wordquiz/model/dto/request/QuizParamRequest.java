package com.jyk.wordquiz.wordquiz.model.dto.request;

import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizParamRequest {
    private String name;
    private String description;
    private String wordBookIds;
    private SharingStatus sharingStatus;
    private List<Long> quizTypeIds; // 퀴즈타입 지정
}
