package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizParamRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.ApiResponseWrapper;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizTypeResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizzesResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.QuizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@Slf4j
public class QuizController {
    @Autowired
    private QuizService quizService;

    @PostMapping
    public ResponseEntity<?> createQuiz(Authentication authentication, @RequestBody QuizParamRequest quizParamRequest) {
        User user = AuthUtil.getCurrentUser(authentication);
        quizService.createQuiz(user, quizParamRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈를 생성했습니다."));
    }

    @GetMapping
    public ResponseEntity<?> getQuizList(Authentication authentication, @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                         @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                         @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort,
                                         @RequestParam(required = false, defaultValue = "ALL", value = "kind") String kind,
                                         @RequestParam(required = false, value = "searchId") Long searchId,
                                         @RequestParam(required = false, value = "quizTypeIds") List<Long> quizTypeIds) {

        User user = AuthUtil.getCurrentUser(authentication);
        QuizzesResponse result = quizService.getQuizList(user, page, criteria, sort.toUpperCase(), kind, searchId, quizTypeIds);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 리스트를 불러왔습니다.", result));
    }

    @GetMapping("/types")
    public ResponseEntity<?> getAvailableQuizTypes() {
        List<QuizTypeResponse> result = quizService.getAvailableQuizTypes();

        return ResponseEntity.ok(ApiResponseWrapper.success("사용 가능한 퀴즈 타입 리스트입니다.", result));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuiz(Authentication authentication, @PathVariable Long quizId) {
        User user = AuthUtil.getCurrentUser(authentication);
        QuizResponse result = quizService.getQuiz(user, quizId);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 상세정보를 불러왔습니다.", result));
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<?> updateQuiz(Authentication authentication, @PathVariable Long quizId, @RequestBody QuizParamRequest quizParamRequest) {
        User user = AuthUtil.getCurrentUser(authentication);

        quizService.updateQuiz(user, quizId, quizParamRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 수정을 성공했습니다."));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<?> deleteQuiz(Authentication authentication, @PathVariable Long quizId) {
        User user = AuthUtil.getCurrentUser(authentication);

        quizService.deleteQuiz(user, quizId);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 삭제를 성공했습니다."));
    }
}
