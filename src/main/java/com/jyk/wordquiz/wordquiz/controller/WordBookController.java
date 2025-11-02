package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordBookRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooksResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.WordBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wordbooks")
@Slf4j
@Tag(name="단어장 관리", description = "단어장 CURD API")
public class WordBookController {
    @Autowired
    private WordBookService wordBookService;

    @Operation(summary = "단어장 목록 조회", description = "단어장 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "단어장 리스트 조회 성공",
                    content = @Content(schema = @Schema(implementation = WordBooksResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<?> getWordBooks(Authentication authentication,
                                          @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                          @RequestParam(required = false, defaultValue = "0", value = "page") int page,

                                          @Parameter(description = "정렬 기준 (id, name, createdAt)", example = "id")
                                          @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,

                                          @Parameter(description = "정렬 방향 (ASC, DESC)", example = "DESC")
                                          @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort) {
        User user = AuthUtil.getCurrentUser(authentication);

        WordBooksResponse result = wordBookService.getWordBooks(user, page, criteria, sort.toUpperCase());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어장 리스트를 불러왔습니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "단어장 추가", description = "단어장을 새로 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "단어장 추가 성공")
    })
    @PostMapping
    public ResponseEntity<?> addWordBook(Authentication authentication,
                                         @Parameter(description = "추가할 단어장 정보") @RequestBody WordBookRequest wordBookReq) {
        User user = AuthUtil.getCurrentUser(authentication);
        wordBookService.saveWordBook(wordBookReq, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어장을 저장했습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "단어장 수정", description = "기존의 단어장 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "단어장 수정 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "단어장 수정 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> putWordBook(@Parameter(description = "단어장 ID") @PathVariable Long id,
                                         Authentication authentication,
                                         @Parameter(description = "수정할 단어장 정보") @RequestBody WordBookRequest wordBookReq) {
        User user = AuthUtil.getCurrentUser(authentication);
        wordBookService.updateWordBook(id, wordBookReq, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어장을 수정했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "단어장 삭제", description = "단어장을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "단어장 삭제 성공")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delWordBook(Authentication authentication,
                                         @Parameter(description = "삭제할 단어장 ID") @PathVariable Long id) {
        User user = AuthUtil.getCurrentUser(authentication);
        wordBookService.deleteWordBook(id, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어장을 삭제했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
