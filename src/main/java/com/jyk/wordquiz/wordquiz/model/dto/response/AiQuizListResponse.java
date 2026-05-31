package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiQuizListResponse {
    private List<Item> problems;

    public List<Item> problems() {
        return problems;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Long wordId;
        private String sentence;
        private String translation;

        public Long wordId() {
            return wordId;
        }

        public String sentence() {
            return sentence;
        }

        public String translation() {
            return translation;
        }
    }
}
