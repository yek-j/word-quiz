package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.model.dto.request.WordBookRequest;
import com.jyk.wordquiz.wordquiz.service.WordBookService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wordbooks")
public class WordBookController {
    @Autowired
    private WordBookService wordBookService;

    @PostMapping
    public ResponseEntity<?> addWordBook(Authentication authentication, @RequestBody WordBookRequest wordBookReq) {
        try {
            String jwtToken = authentication.getCredentials().toString();
            wordBookService.saveWordBook(wordBookReq, jwtToken);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "단어장을 저장했습니다.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "단어장 저장 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
