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
public class WordBooksResponse {
    private List<WordBooks> wordBooks = new ArrayList<>();
    private int totalPage;
}
