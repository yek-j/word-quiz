package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.response.ApiResponseWrapper;
import com.jyk.wordquiz.wordquiz.model.dto.response.LearningOverview;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizAnalysis;
import com.jyk.wordquiz.wordquiz.model.dto.response.WeakWordsAnalysis;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {
    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/quiz")
    public ResponseEntity<?> getQuizAnalysis(Authentication authentication) {
        User user = AuthUtil.getCurrentUser(authentication);

        QuizAnalysis result = analysisService.quizAnalysis(user);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈별 분석을 가져왔습니다.", result));
    }

    @GetMapping("/weak-words")
    public ResponseEntity<?> getWeakWordAnalysis(Authentication authentication,
                                                 @RequestParam(defaultValue = "10") int limit,
                                                 @RequestParam(defaultValue = "50") int maxAccuracy) {
        User user = AuthUtil.getCurrentUser(authentication);

        WeakWordsAnalysis result = analysisService.weakWordsAnalysis(user, limit, maxAccuracy);

        return ResponseEntity.ok(ApiResponseWrapper.success("단어별 취약점 분석을 가져왔습니다.", result));
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getLearningOverview(Authentication authentication) {
        User user = AuthUtil.getCurrentUser(authentication);

        LearningOverview result = analysisService.getLearningOverview(user);

        return ResponseEntity.ok(ApiResponseWrapper.success("전체 학습 통계를 가져왔습니다.", result));
    }

}
