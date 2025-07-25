package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.model.dto.request.QuizAnswerRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizStartRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizAnswerResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizResultResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizSessionResponse;
import com.jyk.wordquiz.wordquiz.service.QuizSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quiz-session")
public class QuizSessionController {
    @Autowired
    private QuizSessionService sessionService;

    @PostMapping
    public ResponseEntity<?> startQuiz(Authentication authentication, @RequestBody QuizStartRequest quizStartRequest) {
        String jwtToken = authentication.getCredentials().toString();
        QuizSessionResponse result = sessionService.startQuiz(jwtToken, quizStartRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "퀴즈를 시작합니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<?> answerQuiz(Authentication authentication,
                                        @PathVariable Long sessionId,
                                        @RequestBody QuizAnswerRequest quizAnswerReq) {
        String jwtToken = authentication.getCredentials().toString();
        QuizAnswerResponse result = sessionService.getIsCorrect(jwtToken, sessionId, quizAnswerReq);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "답변 채점을 완료했습니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<?> quizResult(Authentication authentication, @PathVariable Long sessionId) {
        String jwtToken = authentication.getCredentials().toString();
        QuizResultResponse result = sessionService.getQuizResult(jwtToken, sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "퀴즈 결과입니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
