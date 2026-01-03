package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.FriendRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.FriendRequestResult;
import com.jyk.wordquiz.wordquiz.model.dto.response.FriendsResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.SocialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/social")
@Validated
@Tag(name="친구 관리", description = "친구 관리 API")
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
                                           @Valid @RequestBody FriendRequest friendRequest) {
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

    @Operation(summary = "친구 요청 리스트", description = "요청 온 친구 리스트를 본다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "요청 온 친구 리스트"
            )
    })
    @GetMapping("/friend-requests")
    public ResponseEntity<?> getFriendRequests(Authentication authentication,
                                               @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                               @RequestParam(required = false, defaultValue = "0", value = "page") @Min(0) int page) {
        User user = AuthUtil.getCurrentUser(authentication);

        FriendsResponse result = socialService.getFriendRequestList(user, page);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "친구 요청 온 리스트 입니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 수락", description = "친구 요청이 왔을 때 수락한다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 친구 수락 완료"
            )
    })
    @PostMapping("/friend-requests/{requestUserId}/accept")
    public ResponseEntity<?> friendRequestAccept(Authentication authentication,
                                                 @Parameter(description = "친구 요청한 사용자 ID")
                                                 @PathVariable Long requestUserId) {
        User user = AuthUtil.getCurrentUser(authentication);

        socialService.friendRequestAccept(user, requestUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "친구 수락을 완료했습니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 거절", description = "친구 요청이 왔을 때 거절한다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 친구 수락 거절 완료"
            )
    })
    @PostMapping("/friend-requests/{requestUserId}/reject")
    public ResponseEntity<?> friendRequestReject(Authentication authentication,
                                                 @Parameter(description = "친구 요청한 사용자 ID")
                                                 @PathVariable Long requestUserId) {
        User user = AuthUtil.getCurrentUser(authentication);

        socialService.friendRequestReject(user, requestUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "친구 거절을 완료했습니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 리스트 보기", description = "친구 리스트를 확인한다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "친구 리스트"
            )
    })
    @GetMapping("/friends")
    public ResponseEntity<?> getFriends(Authentication authentication,
                                        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                                        @RequestParam(required = false, defaultValue = "0", value = "page") @Min(0) int page) {
        User user = AuthUtil.getCurrentUser(authentication);

        FriendsResponse result = socialService.getFriendList(user, page);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "친구 리스트 입니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구 삭제", description = "친구를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "친구를 삭제"
            )
    })
    @DeleteMapping("/friend/{deleteFriendId}")
    public ResponseEntity<?> deleteFriend(Authentication authentication,
                                          @Parameter(description = "삭제할 친구 사용자 ID")
                                          @PathVariable Long deleteFriendId) {
        User user = AuthUtil.getCurrentUser(authentication);
        
        socialService.deleteFriend(user, deleteFriendId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "친구 삭제를 완료했습니다.");
        
        return ResponseEntity.ok(response);
    }

}
