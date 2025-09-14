package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizResponse {
    private String name;
    private String createByName;
    private SharingStatus sharingStatus;
    private String description;
    private List<WordBooks> quizWordBooks;
}
