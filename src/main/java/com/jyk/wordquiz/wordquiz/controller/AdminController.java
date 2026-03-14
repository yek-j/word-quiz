package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.PromptRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.AdminUserListResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.ListResultResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.PromptResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
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

    /**
     * Retrieve the paginated and sorted list of users for administrative purposes.
     *
     * @param username optional username substring to filter results by (empty string for no filtering)
     * @param page     zero-based page index to return
     * @param criteria field to sort by (e.g., "id", "username", "createdAt")
     * @param sort     sort direction, either "ASC" or "DESC"
     * @return         a ResponseEntity whose body is a map containing:
     *                 - "status": operation status ("success"),
     *                 - "message": descriptive text,
     *                 - "result": an AdminUserListResponse with the requested page of users
     */
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

    /**
     * Adds a new prompt using the authenticated user and the provided prompt data.
     *
     * @param promptRequest the prompt data to create, including title, content, and metadata
     * @return a map with keys "status" (set to "success") and "message" describing the result
     */

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

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "프롬프트 추가 성공입니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve a paginated list of prompts for administrative use.
     *
     * Supports optional filtering by prompt name and type, and configurable pagination and sorting.
     *
     * @param promptName optional substring to filter prompts by name
     * @param promptType optional prompt type to filter results
     * @param page zero-based page index for pagination
     * @param criteria field to sort by (e.g., "id", "promptName", "promptType", "createdAt", "updatedAt", "createdBy", "lastModifiedBy")
     * @param sort sort direction, either "ASC" or "DESC"
     * @return a map with keys "status", "message", and "result" where "result" is a ListResultResponse containing the requested page of prompts
     */
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

    /**
     * Retrieve a single prompt by its ID.
     *
     * @param promptId the ID of the prompt to retrieve
     * @return a ResponseEntity whose body is a map containing:
     *         - "status": "success"
     *         - "message": a descriptive text
     *         - "result": the retrieved PromptResponse
     */
    @Operation(summary = "프롬프트 단건 조회", description = "특정 프롬프트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프롬프트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 프롬프트")
    })
    @GetMapping("/prompt/{promptId}")
    public ResponseEntity<?> getPrompt(@Parameter(description = "조회할 프롬프트 ID") @PathVariable Long promptId) {
        PromptResponse result = adminService.getPrompt(promptId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "프롬프트 결과입니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    /**
     * Disable the prompt identified by the given ID.
     *
     * Marks the specified prompt as disabled and returns a standard success response.
     *
     * @param promptId the ID of the prompt to disable
     * @return a map containing "status" set to "success" and "message" confirming the prompt deletion
     */
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

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "프롬프트 삭제 성공입니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the content of a specific prompt.
     *
     * @param promptId the identifier of the prompt to update
     * @param promptRequest the new prompt data to apply
     * @return a map with keys "status" set to "success" and "message" describing the outcome
     */
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

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "프롬프트 수정 성공입니다.");

        return ResponseEntity.ok(response);
    }


    // TODO: 퀴즈, 단어장 설정 기능
}
