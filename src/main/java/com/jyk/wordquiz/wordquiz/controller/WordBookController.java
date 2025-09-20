package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordBookRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooksResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.WordBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wordbooks")
@Slf4j
public class WordBookController {
    @Autowired
    private WordBookService wordBookService;

    @GetMapping
    public ResponseEntity<?> getWordBooks(Authentication authentication,
                                          @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                          @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                          @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort) {
        User user = AuthUtil.getCurrentUser(authentication);

        WordBooksResponse result = wordBookService.getWordBooks(user, page, criteria, sort.toUpperCase());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어장 리스트를 불러왔습니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<?> addWordBook(Authentication authentication, @RequestBody WordBookRequest wordBookReq) {
        User user = AuthUtil.getCurrentUser(authentication);
        wordBookService.saveWordBook(wordBookReq, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어장을 저장했습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> putWordBook(@PathVariable Long id, Authentication authentication, @RequestBody WordBookRequest wordBookReq) {
        User user = AuthUtil.getCurrentUser(authentication);
        wordBookService.updateWordBook(id, wordBookReq, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어장을 수정했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delWordBook(Authentication authentication, @PathVariable Long id) {
        User user = AuthUtil.getCurrentUser(authentication);
        wordBookService.deleteWordBook(id, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어장을 삭제했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
