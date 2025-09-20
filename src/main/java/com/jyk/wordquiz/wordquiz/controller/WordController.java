package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.common.excel.UploadExcel;
import com.jyk.wordquiz.wordquiz.common.exception.ErrorResponse;
import com.jyk.wordquiz.wordquiz.model.dto.request.UpdateWordRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordCheckRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordCheckResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.Words;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordsResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name="단어 관리", description = "단어장 내 단어 CURD API")
public class WordController {
    @Autowired
    private WordService wordService;

    @Operation(summary = "단어 목록 조회", description = "특정 단어장의 단어 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "단어 리스트 조회 성공",
                    content = @Content(schema = @Schema(implementation = WordsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "단어장 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "단어장 찾을 수 없음"
            )
    })
    @GetMapping
    public ResponseEntity<?> getWords(Authentication authentication,
                                     @Parameter(description = "단어장 ID", example = "1")
                                     @PathVariable Long wordBookId,

                                     @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                     @RequestParam(required = false, defaultValue = "0", value = "page") int page,

                                     @Parameter(description = "정렬 기준 (id, term, cratedAt", example = "id")
                                     @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,

                                     @Parameter(description = "정렬 방향 (ASC, DESC)", example = "DESC")
                                     @RequestParam(required= false, defaultValue = "DESC", value = "sort") String sort) throws AccessDeniedException {
        User user = AuthUtil.getCurrentUser(authentication);
        WordsResponse result = wordService.getWords(wordBookId, user, page, criteria, sort.toUpperCase());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어 리스트를 불러왔습니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "단어 추가",
            description = "단어장에 새로운 단어를 추가합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "단어 추가 성공"),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 단어",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "단어장 접근 권한 없음")
    })
    @PostMapping
    public ResponseEntity<?> addWord(Authentication authentication,
                                     @Parameter(description = "단어장 ID") @PathVariable Long wordBookId,
                                     @Parameter(description= "추가할 단어 정보") @RequestBody WordRequest wordReq
    ) throws AccessDeniedException {
        User user = AuthUtil.getCurrentUser(authentication);

        wordService.saveWord(wordBookId, wordReq, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어를 저장했습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "단어 수정",
            description = "기존의 단어를 수정하거나 다른 단어장으로 이동시킨다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "단어 수정 성공"),
            @ApiResponse(
                    responseCode = "403",
                    description = "단어장 또는 단어 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "단어장 또는 단어 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{wordId}")
    public ResponseEntity<?> putWord(Authentication authentication,
                                     @Parameter(description = "현재 단어장 ID", example = "1")
                                     @PathVariable Long wordBookId,
                                     @Parameter(description = "수정할 단어 ID", example = "10")
                                     @PathVariable Long wordId,
                                     @Parameter(description = "수정할 단어 정보(다른 단어장 이동 가능)")
                                     @RequestBody UpdateWordRequest updateWordReq) throws AccessDeniedException {
        User user = AuthUtil.getCurrentUser(authentication);
        wordService.updateWord(wordBookId, wordId, updateWordReq, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어를 수정했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "단어 삭제",
            description = "단어장에서 특정 단어를 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "단어 삭제 성공"),
            @ApiResponse(
                    responseCode = "403",
                    description = "단어장 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "단어를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{wordId}")
    public ResponseEntity<?> delWord(Authentication authentication,
                                     @Parameter(description = "단어장 ID") @PathVariable Long wordBookId,
                                     @Parameter(description = "삭제할 단어 ID") @PathVariable Long wordId
    ) throws AccessDeniedException {
        User user = AuthUtil.getCurrentUser(authentication);

        wordService.deleteWord(wordBookId, wordId, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어를 삭제했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "단어 중복 체크",
            description = "단어장에 동일한 단어가 이미 존재하는지 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "중복 체크 완료",
                    content = @Content(schema = @Schema(implementation = WordCheckRequest.class))
            ),
            @ApiResponse(responseCode = "403", description = "단어장 접근 권한 없음")
    })
    @PostMapping("/duplicates")
    public ResponseEntity<?> duplicateCheck(Authentication authentication,
                                            @Parameter(description = "단어장 ID") @PathVariable Long wordBookId,
                                            @Parameter(description = "중복 체크할 단어") @RequestBody WordCheckRequest wordCheckReq
    ) throws AccessDeniedException {
        User user = AuthUtil.getCurrentUser(authentication);

        WordCheckResponse result = wordService.wordCheck(wordCheckReq, wordBookId, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어 중복체크를 완료했습니다.");
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Excel 파일로 단어 일괄 업로드",
            description = "Excel 파일을 업로드하여 여러 단어를 한 번에 추가합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식"),
            @ApiResponse(responseCode = "403", description = "단어장 접근 권한 없음")
    })
    @PostMapping("/file")
    public ResponseEntity<?> UploadWordFile(Authentication authentication,
                                            @Parameter(description = "단어장 ID") @PathVariable Long wordBookId,
                                            @Parameter(
                                                    description = "업로드할 Excel 파일 (.xlsx, .xls)",
                                                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
                                            ) @RequestParam("file") MultipartFile file) throws IOException {
        User user = AuthUtil.getCurrentUser(authentication);
        Map<String, String> words = UploadExcel.uploadWordExcel(file);

        List<Words> existingWord = wordService.saveExcelData(words, wordBookId, user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "단어 저장을 완료했습니다.");
        response.put("existingWord", existingWord);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "단어 업로드 템플릿 다운로드",
            description = "단어 일괄 업로드용 Excel 템플릿 파일을 다운로드합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "템플릿 다운로드 성공",
                    content = @Content(
                            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
            )
    })
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
