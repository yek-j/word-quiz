package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.model.dto.response.BlankQuizResponse;
import com.jyk.wordquiz.wordquiz.model.entity.Word;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIQuestionService {
    private final ChatClient chatClient;

    /**
     * Creates an AIQuestionService and initializes its ChatClient from the provided builder.
     *
     * @param geminiChatClientBuilder a configured ChatClient.Builder used to construct the internal ChatClient
     */
    public AIQuestionService(ChatClient.Builder geminiChatClientBuilder) {
        this.chatClient = geminiChatClientBuilder.build();
    }

    /**
     * Generate a single blank-cloze sentence quiz for the given word.
     *
     * @param word the Word containing the term and its description to use when constructing the quiz
     * @return a BlankQuizResponse with a `sentence` where the term is replaced by `___` and a `translation` with the complete translated sentence
     */
    public BlankQuizResponse generationBlankQuestion(Word word) {
        return chatClient
                .prompt()
                .user(u -> u.text("""
                        다음 단어로 빈칸 문제(문장) 하나를 만들어주세요.
                        단어: {word}, 뜻: {meaning}
                        
                        규칙:
                        - sentence: 단어 부분을 ___로 대체한 문장
                        - translation: 빈칸 없이 완전한 문장의 해석
                        """)
                        .param("word", word.getTerm())
                        .param("meaning", word.getDescription()))
                .call()
                .entity(BlankQuizResponse.class);
    }
}