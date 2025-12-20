package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.response.UsersResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name="사용자 관리", description = "사용자 관리 API")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "사용자 조회", description = "사용자 이름으로 친구 추가할 목록을 검색한다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 목록 조회 성공"
            )
    })
    @GetMapping("/search")
    public ResponseEntity<?> getUsers(Authentication authentication,
                                      @Parameter(description = "검색할 사용자 이름")
                                      @RequestParam(defaultValue = "") String username,

                                      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                      @RequestParam(required = false, defaultValue = "0", value = "page") int page) {
        User user = AuthUtil.getCurrentUser(authentication);

        UsersResponse result = userService.getUserList(user, username, page);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "사용자 이름 친구 검색 결과입니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

}
