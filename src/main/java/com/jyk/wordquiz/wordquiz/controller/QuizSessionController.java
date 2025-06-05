package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.model.dto.request.QuizStartRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quiz-session")
public class QuizSessionController {

    @PostMapping
    public ResponseEntity<?> startQuiz(Authentication authentication, @RequestBody QuizStartRequest quizStartRequest) {
        // TODO: 퀴즈 유형에 맞는 퀴즈 출제
        return null;
    }

}
