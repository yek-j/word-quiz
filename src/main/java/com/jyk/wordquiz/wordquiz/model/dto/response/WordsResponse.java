package com.jyk.wordquiz.wordquiz.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WordsResponse {
    private List<Words> words = new ArrayList<>();
    private int totalPage;
}
