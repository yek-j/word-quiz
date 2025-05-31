package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.excel.UploadExcel;
import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.DuplicationWordException;
import com.jyk.wordquiz.wordquiz.model.dto.request.UpdateWordRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordCheckRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordCheckResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.Words;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordsResponse;
import com.jyk.wordquiz.wordquiz.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wordbooks/{wordBookId}/words")
@Slf4j
public class WordController {
    @Autowired
    private WordService wordService;

    @GetMapping
    public ResponseEntity<?> getWords(Authentication authentication,
                                         @PathVariable Long wordBookId,
                                         @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                         @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                         @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort) throws AccessDeniedException {
        String jwtToken = authentication.getCredentials().toString();
        WordsResponse result = wordService.getWords(wordBookId, jwtToken, page, criteria, sort.toUpperCase());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어 리스트를 불러왔습니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<?> addWord(Authentication authentication,
                                     @PathVariable Long wordBookId, @RequestBody WordRequest wordReq) throws AccessDeniedException {
        String jwtToken = authentication.getCredentials().toString();

        wordService.saveWord(wordBookId, wordReq, jwtToken);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어를 저장했습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{wordId}")
    public ResponseEntity<?> putWord(Authentication authentication,
                                     @PathVariable Long wordBookId, @PathVariable Long wordId,
                                     @RequestBody UpdateWordRequest updateWordReq) throws AccessDeniedException {
        String jwtToken = authentication.getCredentials().toString();
        wordService.updateWord(wordBookId, wordId, updateWordReq, jwtToken);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어를 수정했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<?> delWord(Authentication authentication,
                                     @PathVariable Long wordBookId, @PathVariable Long wordId) throws AccessDeniedException {
        String jwtToken = authentication.getCredentials().toString();

        wordService.deleteWord(wordBookId, wordId, jwtToken);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어를 삭제했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/duplicates")
    public ResponseEntity<?> duplicateCheck(Authentication authentication,
                                            @PathVariable Long wordBookId, @RequestBody WordCheckRequest wordCheckReq) throws AccessDeniedException {
        String jwtToken = authentication.getCredentials().toString();

        WordCheckResponse result = wordService.wordCheck(wordCheckReq, wordBookId, jwtToken);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어 중복체크를 완료했습니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/file")
    public ResponseEntity<?> UploadWordFile(Authentication authentication,
                                            @PathVariable Long wordBookId, @RequestParam("file") MultipartFile file) throws IOException {
        String jwtToken = authentication.getCredentials().toString();
        Map<String, String> words = UploadExcel.uploadWordExcel(file);

        List<Words> existingWord = wordService.saveExcelData(words, wordBookId,jwtToken);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어 저장을 완료했습니다.");
        response.put("existingWord", existingWord);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/template")
    public ResponseEntity<Resource> downloadTemplate() throws IOException {
        Resource resource = new ClassPathResource("static/templates/word-import-template.xlsx");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"word-template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
