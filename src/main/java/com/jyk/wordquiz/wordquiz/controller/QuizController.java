package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.CreateQuizRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizzesResponse;
import com.jyk.wordquiz.wordquiz.service.QuizService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes")
@Slf4j
public class QuizController {
    @Autowired
    private QuizService quizService;

    @PostMapping
    public ResponseEntity<?> createQuiz(Authentication authentication, @RequestBody CreateQuizRequest createQuizRequest) {
        String jwtToken = authentication.getCredentials().toString();
        quizService.createQuiz(jwtToken, createQuizRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "퀴즈를 생성했습니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getQuizList(Authentication authentication, @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                         @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                         @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort,
                                         @RequestParam(required = false, defaultValue = "all", value = "kind") String kind) {
        
        String jwtToken = authentication.getCredentials().toString();
        QuizzesResponse result = quizService.getQuizList(jwtToken, page, criteria, sort.toUpperCase(), kind);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "퀴즈 리스트를 불러왔습니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
