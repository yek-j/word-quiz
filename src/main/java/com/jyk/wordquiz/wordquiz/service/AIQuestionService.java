package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.model.dto.response.AiQuizListResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.AiQuizResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizProblem;
import com.jyk.wordquiz.wordquiz.model.entity.Prompt;
import com.jyk.wordquiz.wordquiz.model.entity.Word;
import com.jyk.wordquiz.wordquiz.repository.PromptRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class AIQuestionService {
    private final ChatClient chatClient;
    private final PromptRepository promptRepository;

    /**
     * AIQuestionService를 생성하고, 제공된 빌더를 사용하여 해당 ChatClient를 초기화합니다.
     *
     * @param geminiChatClientBuilder 내부 ChatClient를 생성하는 데 사용되는 구성된 ChatClient.Builder
     */
    public AIQuestionService(ChatClient.Builder geminiChatClientBuilder, PromptRepository promptRepository) {
        this.chatClient = geminiChatClientBuilder.build();
        this.promptRepository = promptRepository;
    }

    /**
     * DB에 있는 퀴즈 타입의 Default 프롬프트를 이용해 주어진 단어들F로 퀴즈를 생성한다.
     * AI 퀴즈는 정답이 단어(term)를 작성하는 문제만을 생성한다.
     * @param words 퀴즈를 생성해야 할 단어들
     * @return LLM으로 생성한 퀴즈들을 반환한다.
     */
    public List<QuizProblem> generationAiQuestions(List<Word> words, Long quizTypeId) {
        Prompt prompt = promptRepository.findByPromptTypeId(quizTypeId)
                .orElseThrow(() -> new IllegalArgumentException("사용할 수 없는 퀴즈 타입 입니다."));

        // 프롬프트 템플릿의 {words} 자리에 들어갈 단어 목록 (LLM이 wordId를 그대로 응답에 담아주도록)
        String wordsParam = words.stream()
                .map(w -> "- wordId=" + w.getId() + ", term=" + w.getTerm() + ", meaning=" + w.getDescription())
                .collect(Collectors.joining("\n"));

        AiQuizListResponse response = chatClient.prompt()
                .user(u -> u.text(prompt.getContent()).param("words", wordsParam))
                .call()
                .entity(AiQuizListResponse.class);

        Map<Long, Word> wordById = words.stream()
                .collect(Collectors.toMap(Word::getId, w -> w));

        return response.problems().stream()
                .filter(item -> wordById.containsKey(item.wordId()))
                .map(item -> new QuizProblem(
                        item.wordId(),
                        item.sentence(),
                        wordById.get(item.wordId()).getTerm(),
                        item.translation(),
                        null
                ))
                .toList();
    }

    /**
     * 샘플 단어로 프롬프트를 실행해 LLM 응답을 받아온다.
     * 어드민의 프롬프트 검증("검증" 버튼)에서 사용한다.
     * @param promptContent 검증할 프롬프트 템플릿 (예: "...{word}...{meaning}...")
     * @param sampleTerm 샘플 단어
     * @param sampleMeaning 샘플 단어의 의미
     * @return LLM이 반환한 AiQuizResponse (파싱 실패 시 예외)
     */
    public AiQuizResponse runWithSample(String promptContent, String sampleTerm, String sampleMeaning) {
        return chatClient
                .prompt()
                .user(u -> u.text(promptContent)
                        .param("word", sampleTerm)
                        .param("meaning", sampleMeaning))
                .call()
                .entity(AiQuizResponse.class);
    }
}