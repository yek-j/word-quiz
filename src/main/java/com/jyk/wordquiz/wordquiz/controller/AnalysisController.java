package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.model.dto.response.LearningOverview;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizAnalysis;
import com.jyk.wordquiz.wordquiz.model.dto.response.WeekWordsAnalysis;
import com.jyk.wordquiz.wordquiz.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {
    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/quiz")
    public ResponseEntity<?> getQuizAnalysis(Authentication authentication) {
        String jwtToken = authentication.getCredentials().toString();

        QuizAnalysis result = analysisService.quizAnalysis(jwtToken);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "퀴즈별 분석을 가져왔습니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/week-words")
    public ResponseEntity<?> getWeekWordAnalysis(Authentication authentication,
                                                 @RequestParam(defaultValue = "10") int limit,
                                                 @RequestParam(defaultValue = "50") int maxAccuracy) {
        String jwtToken = authentication.getCredentials().toString();

        WeekWordsAnalysis result = analysisService.weekWordsAnalysis(jwtToken, limit, maxAccuracy);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어별 취약점 분석을 가져왔습니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getLearningOverview(Authentication authentication) {
        String jwtToken = authentication.getCredentials().toString();

        LearningOverview result = analysisService.getLearningOverview(jwtToken);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "전체 학습 통계를 가져왔습니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

}
