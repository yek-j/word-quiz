package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizAnswerRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizStartRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.ApiResponseWrapper;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizAnswerResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizResultResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizSessionResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.QuizSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quiz-session")
public class QuizSessionController {
    @Autowired
    private QuizSessionService sessionService;

    @PostMapping
    public ResponseEntity<?> startQuiz(Authentication authentication, @RequestBody QuizStartRequest quizStartRequest) {
        User user = AuthUtil.getCurrentUser(authentication);
        QuizSessionResponse result = sessionService.startQuiz(user, quizStartRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈를 시작합니다.", result));
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<?> answerQuiz(Authentication authentication,
                                        @PathVariable Long sessionId,
                                        @RequestBody QuizAnswerRequest quizAnswerReq) {
        User user = AuthUtil.getCurrentUser(authentication);
        QuizAnswerResponse result = sessionService.getIsCorrect(user, sessionId, quizAnswerReq);

        return ResponseEntity.ok(ApiResponseWrapper.success("답변 채점을 완료했습니다.", result));
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<?> quizResult(Authentication authentication, @PathVariable Long sessionId) {
        User user = AuthUtil.getCurrentUser(authentication);
        QuizResultResponse result = sessionService.getQuizResult(user, sessionId);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 결과입니다.", result));
    }
}
