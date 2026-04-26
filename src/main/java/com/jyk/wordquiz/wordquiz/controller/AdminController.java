package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.ConfigRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.PromptRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.PromptValidateRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizTypeRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.UserRoleRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUserListResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.ApiResponseWrapper;
import com.jyk.wordquiz.wordquiz.model.dto.response.ListResultResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.PromptResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.PromptValidateResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizTypeResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

        return ResponseEntity.ok(ApiResponseWrapper.success("관리자용 사용자 리스트 결과입니다.", result));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<?> setRole(@PathVariable Long userId,
                                     @Valid @RequestBody UserRoleRequest userRoleRequest) {

        adminService.setUserRole(userId, userRoleRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("사용자 권한 변경 성공입니다."));
    }

    @Operation(summary = "퀴즈 타입 추가", description = "퀴즈 타입을 추가하는 기능입니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프롬프트 추가 성공"
            )
    })
    @PostMapping("/quizType")
    public ResponseEntity<?> addQuizType(Authentication authentication,
                                         @Parameter(description = "추가할 퀴즈 타입 데이터")
                                         @RequestBody QuizTypeRequest quizTypeRequest) {
        User user = AuthUtil.getCurrentUser(authentication);
        adminService.addQuizType(user, quizTypeRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 타입 추가 성공입니다."));
    }

    @Operation(summary = "퀴즈 타입 조회", description = "퀴즈타입 리스트입니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "퀴즈 타입 목록 조회 성공"
            )
    })
    @GetMapping("/quizType")
    public ResponseEntity<?> getQuizTypeList (@Parameter(description = "검색할 퀴즈 타입 이름")
                                              @RequestParam(defaultValue = "") String quizTypeName,
                                              @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                              @RequestParam(required = false, defaultValue = "0", value = "page") int page,
                                              @Parameter(description = "정렬 기준 (id, quizTypeName, useAi, createdAt, updatedAt, createdBy, lastModifiedBy)", example = "id")
                                              @RequestParam(required = false, defaultValue = "id", value = "orderby") String criteria,
                                              @Parameter(description = "정렬 방향 (ASC, DESC)", example = "DESC")
                                              @RequestParam(required= false, defaultValue = "ASC", value = "sort") String sort) {
        ListResultResponse result = adminService.getQuizTypeList(page, criteria, sort, quizTypeName);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 타입 리스트 결과입니다.", result));
    }

    @Operation(summary = "퀴즈 타입 단건 조회", description = "특정 퀴즈 타입을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 타입 조회 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 퀴즈 타입")
    })
    @GetMapping("/quizType/{quizTypeId}")
    public ResponseEntity<?> getQuizType(@Parameter(description = "조회할 퀴즈 타입 ID") @PathVariable Long quizTypeId) {
        QuizTypeResponse result = adminService.getQuizType(quizTypeId);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 타입 결과입니다.", result));
    }

    @Operation(summary = "퀴즈 타입 수정", description = "특정 퀴즈 타입의 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 타입 수정 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 퀴즈 타입")
    })
    @PutMapping("/quizType/{quizTypeId}")
    public ResponseEntity<?> updateQuizType(Authentication authentication,
                                            @Parameter(description = "수정할 퀴즈 타입 ID") @PathVariable Long quizTypeId,
                                            @Parameter(description = "수정할 퀴즈 타입 데이터") @RequestBody QuizTypeRequest quizTypeRequest) {
        User user = AuthUtil.getCurrentUser(authentication);
        adminService.updateQuizType(user, quizTypeId, quizTypeRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 타입 수정 성공입니다."));
    }

    @Operation(summary = "퀴즈 타입 삭제", description = "특정 퀴즈 타입을 삭제합니다. 매핑된 프롬프트가 있으면 함께 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 타입 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 퀴즈 타입")
    })
    @DeleteMapping("/quizType/{quizTypeId}")
    public ResponseEntity<?> deleteQuizType(Authentication authentication,
                                            @Parameter(description = "삭제할 퀴즈 타입 ID") @PathVariable Long quizTypeId) {
        User user = AuthUtil.getCurrentUser(authentication);
        adminService.deleteQuizType(user, quizTypeId);

        return ResponseEntity.ok(ApiResponseWrapper.success("퀴즈 타입 삭제 성공입니다."));
    }

    @Operation(summary = "프롬프트 검증", description = "샘플 단어로 LLM을 호출해 프롬프트가 구조화된 출력을 내는지 검증합니다. 저장 전 '검증' 버튼에서 사용합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검증 결과 (valid=true/false)")
    })
    @PostMapping("/prompt/validate")
    public ResponseEntity<?> validatePrompt(@Valid @RequestBody PromptValidateRequest request) {
        PromptValidateResponse result = adminService.validatePrompt(request.getContent());
        return ResponseEntity.ok(ApiResponseWrapper.success("프롬프트 검증 결과입니다.", result));
    }

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
        User user = AuthUtil.getCurrentUser(authentication);
        adminService.addPrompt(user, promptRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("프롬프트 추가 성공입니다."));
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

        return ResponseEntity.ok(ApiResponseWrapper.success("프롬프트 리스트 결과입니다.", result));
    }

    @Operation(summary = "프롬프트 단건 조회", description = "특정 프롬프트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프롬프트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 프롬프트")
    })
    @GetMapping("/prompt/{promptId}")
    public ResponseEntity<?> getPrompt(@Parameter(description = "조회할 프롬프트 ID") @PathVariable Long promptId) {
        PromptResponse result = adminService.getPrompt(promptId);

        return ResponseEntity.ok(ApiResponseWrapper.success("프롬프트 결과입니다.", result));
    }

    @Operation(summary = "프롬프트 비활성화", description = "특정 프롬프트를 비활성화(disabled=true)합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프롬프트 비활성화 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 프롬프트")
    })
    @DeleteMapping("/prompt/{promptId}")
    public ResponseEntity<?> deletePrompt(Authentication authentication,
                                          @Parameter(description = "비활성화할 프롬프트 ID") @PathVariable Long promptId) {
        User user = AuthUtil.getCurrentUser(authentication);
        adminService.deletePrompt(user, promptId);

        return ResponseEntity.ok(ApiResponseWrapper.success("프롬프트 삭제 성공입니다."));
    }

    @Operation(summary = "프롬프트 수정", description = "특정 프롬프트의 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프롬프트 수정 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 프롬프트")
    })
    @PutMapping("/prompt/{promptId}")
    public ResponseEntity<?> updatePrompt(Authentication authentication,
                                          @Parameter(description = "수정할 프롬프트 ID") @PathVariable Long promptId,
                                          @Parameter(description = "수정할 프롬프트 데이터") @RequestBody PromptRequest promptRequest) {
        User user = AuthUtil.getCurrentUser(authentication);
        adminService.updatePrompt(user, promptId, promptRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("프롬프트 수정 성공입니다."));
    }

    @Operation(summary = "설정 수정", description = "시스템 설정을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "설정 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패)"),
            @ApiResponse(responseCode = "500", description = "Config가 초기화되지 않은 경우")
    })
    @PutMapping("/config")
    public ResponseEntity<?> updateConfig(@Parameter(description = "수정할 설정 데이터") @Valid @RequestBody ConfigRequest configRequest) {
        adminService.updateConfig(configRequest);
        return ResponseEntity.ok(ApiResponseWrapper.success("설정 수정 성공입니다."));
    }
}
