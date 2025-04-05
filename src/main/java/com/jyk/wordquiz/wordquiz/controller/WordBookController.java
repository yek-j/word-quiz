package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.exception.WordBookNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordBookRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooksResponse;
import com.jyk.wordquiz.wordquiz.service.WordBookService;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wordbooks")
public class WordBookController {
    @Autowired
    private WordBookService wordBookService;

    @GetMapping
    public ResponseEntity<?> getWordBooks(Authentication authentication,
                                          @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                          @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                          @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort) {
        try {
            String jwtToken = authentication.getCredentials().toString();

            WordBooksResponse result = wordBookService.getWordBooks(jwtToken, page, criteria, sort.toUpperCase());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "단어장 리스트를 불러왔습니다.");
            response.put("result", result);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "단어장 리스트를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<?> putWordBook(@PathVariable Long id, Authentication authentication, @RequestBody WordBookRequest wordBookReq) {
        try {
            String jwtToken = authentication.getCredentials().toString();
            wordBookService.updateWordBook(id, wordBookReq, jwtToken);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "단어장을 수정했습니다.");

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch(WordBookNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (IllegalAccessException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch(Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "단어장 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delWordBook(Authentication authentication, @PathVariable Long id) {
        try {
            String jwtToken = authentication.getCredentials().toString();
            wordBookService.deleteWordBook(id, jwtToken);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "단어장을 삭제했습니다.");

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(WordBookNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch(Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "단어장 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

    }
}
