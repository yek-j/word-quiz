package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.FriendRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.FriendRequestResult;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.SocialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/social")
public class SocialController {
    @Autowired
    private SocialService socialService;

    @Operation(summary = "친구 요청", description = "사용자 이름으로 친구 요청을 한다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "친구 요청 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "본인에게 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않은 사용자에게 친구 요청"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 친구이거나 이미 요청"
            )
    })
    @PostMapping("/friend-requests")
    public ResponseEntity<?> friendRequest(Authentication authentication,
                                           @RequestBody FriendRequest friendRequest) {
        User user = AuthUtil.getCurrentUser(authentication);

        FriendRequestResult result = socialService.friendRequest(user, friendRequest);

        Map<String, Object> response = new HashMap<>();


        if (result.getCode() == HttpStatus.CREATED) {
            response.put("status", "success");
        } else {
            response.put("status", "fail");
        }

        response.put("message", result.getMessage());

        return ResponseEntity.status(result.getCode()).body(response);
    }
}
