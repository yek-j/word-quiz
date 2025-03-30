package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.exception.WordBookNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordRequest;
import com.jyk.wordquiz.wordquiz.service.WordService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wordbooks/{wordBookId}/words")
public class WordController {
    @Autowired
    private WordService wordService;

    @PostMapping
    public ResponseEntity<?> addWord(Authentication authentication,
                                     @PathVariable Long wordBookId, @RequestBody WordRequest wordReq) {
        try {
            String jwtToken = authentication.getCredentials().toString();

            wordService.saveWord(wordBookId, wordReq, jwtToken);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "단어를 저장했습니다.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch(WordBookNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch(EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "단어 저장 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
