package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.model.dto.request.PromptRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUserListResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.ListResultResponse;
import com.jyk.wordquiz.wordquiz.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "관리자용 사용자 조회", description = "사용자 리스트입니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 목록 조회 성공"
            )
    })
    @GetMapping("/users")
    public ResponseEntity<?> getAllUserList(@Parameter(description = "검색할 사용자 이름")
                                            @RequestParam(defaultValue = "") String username,
                                            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                            @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                            @Parameter(description = "정렬 기준 (id, username, createdAt)", example = "id")
                                            @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                            @Parameter(description = "정렬 방향 (ASC, DESC)", example = "DESC")
                                            @RequestParam(required= false, defaultValue = "ASC", value = "sort") String sort) {

        AdminUserListResponse result = adminService.getAllUsers(page, criteria, sort, username);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "관리자용 사용자 리스트 결과입니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    // TODO: 사용자 관리(권한 등록, 차단 등)

    // TODO: 프롬프트 관리
    @Operation(summary = "프롬프트 추가", description = "프롬프트 추가하는 기능입니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프롬프트 추가 성공"
            )
    })
    @PostMapping("/prompt")
    public ResponseEntity<?> addPrompt(Authentication authentication,
                                       @Parameter(description = "추가할 프롬프트 데이터")
                                       @RequestBody PromptRequest promptRequest) {

        adminService.addPrompt(promptRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "프롬프트 추가 성공입니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "관리자용 프롬프트 조회", description = "프롬프트 리스트입니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프롬프트 목록 조회 성공"
            )
    })
    @GetMapping("/prompt")
    public ResponseEntity<?> getPromptList(@Parameter(description = "검색할 프롬프트 이름")
                                           @RequestParam(defaultValue = "") String promptName,
                                           @Parameter(description = "검색할 프롬프트 타입")
                                           @RequestParam(defaultValue = "") String promptType,
                                           @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                           @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                           @Parameter(description = "정렬 기준 (id, promptName, promptType, createdAt, updatedAt, createdBy, lastModifiedBy)", example = "id")
                                           @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                           @Parameter(description = "정렬 방향 (ASC, DESC)", example = "DESC")
                                           @RequestParam(required= false, defaultValue = "ASC", value = "sort") String sort) {
        ListResultResponse result = adminService.getPromptList(page, criteria, sort, promptName, promptType);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "프롬프트 리스트 결과입니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/prompt")
    public ResponseEntity<?> deletePrompt() {
        return null;
    }

    @PutMapping("/prompt")
    public ResponseEntity<?> updatePrompt() {
        return null;
    }


    // TODO: 퀴즈, 단어장 설정 기능
}
