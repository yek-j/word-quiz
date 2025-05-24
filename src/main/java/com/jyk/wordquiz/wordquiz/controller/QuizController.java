package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.CreateQuizRequest;
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
        try {
            String jwtToken = authentication.getCredentials().toString();
            quizService.createQuiz(jwtToken, createQuizRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "퀴즈를 생성했습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch(EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (AuthenticatedUserNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "세션 정보를 확인할 수 없습니다. 보안을 위해 다시 로그인해주세요.");
            log.error("인증된 사용자를 찾을 수 없음 - 심각한 무결성 오류. userId: {}", e.getUserId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "단어장 저장 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getQuizList(Authentication authentication, @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                         @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                         @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort,
                                         @RequestParam(required = false, defaultValue = "all", value = "kind") String kind) {
        try {

        } catch (Exception e) {

        }
        return null;
    }
}
