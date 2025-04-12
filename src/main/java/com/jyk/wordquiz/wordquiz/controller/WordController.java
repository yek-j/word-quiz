package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.model.dto.request.UpdateWordRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordsResponse;
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

    @GetMapping
    public ResponseEntity<?> getWords(Authentication authentication,
                                         @PathVariable Long wordBookId,
                                         @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                         @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                         @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort) {
        try {
            String jwtToken = authentication.getCredentials().toString();
            WordsResponse result = wordService.getWords(wordBookId, jwtToken, page, criteria, sort.toUpperCase());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "단어 리스트를 불러왔습니다.");
            response.put("result", result);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException e) {
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
            error.put("message", "단어 리스트를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

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

    @PutMapping("/{wordId}")
    public ResponseEntity<?> putWord(Authentication authentication,
                                     @PathVariable Long wordBookId, @PathVariable Long wordId,
                                     @RequestBody UpdateWordRequest updateWordReq) {
        try {
            String jwtToken = authentication.getCredentials().toString();
            wordService.updateWord(wordBookId, wordId, updateWordReq, jwtToken);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "단어를 수정했습니다.");

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch(Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "단어 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<?> delWord(Authentication authentication,
                                     @PathVariable Long wordBookId, @PathVariable Long wordId) {
        try {
            String jwtToken = authentication.getCredentials().toString();

            wordService.deleteWord(wordBookId, wordId, jwtToken);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "단어를 삭제했습니다.");

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch(Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "단어 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
