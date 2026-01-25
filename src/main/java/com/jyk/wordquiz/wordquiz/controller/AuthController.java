package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.*;
import com.jyk.wordquiz.wordquiz.model.dto.response.LoginResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.UserInfoResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> siginUp(@RequestBody SignupRequest signupReq) {
        authService.siginup(signupReq);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "회원가입이 성공적으로 완료되었습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(loginRequest);

        // refresh token
        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        loginResponse.setRefreshToken("");

        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("message", loginResponse);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication,
                                    HttpServletResponse response) {
        User user = AuthUtil.getCurrentUser(authentication);

        // redis에서 refreshToken 삭제
        authService.deleteRefreshToken(user.getId());

        // 쿠키 만료 시키기
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok("로그아웃 완료");
    }

    @GetMapping("/me")
    public ResponseEntity<?> userInfo(Authentication authentication) {
        User user = AuthUtil.getCurrentUser(authentication);
        UserInfoResponse userInfo = authService.getUserInfo(user);

        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(Authentication authentication, @RequestBody UserInfoRequest userInfoRequest) {
        User user = AuthUtil.getCurrentUser(authentication);

        authService.updateUserInfo(user, userInfoRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "사용자 정보가 성공적으로 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePwd changePwd) {
        User user = AuthUtil.getCurrentUser(authentication);
        authService.changePassword(user, changePwd);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "비밀번호가 성공적으로 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(@RequestBody @Valid DeleteAccountRequest deleteAccountRequest, Authentication authentication) {
        User user = AuthUtil.getCurrentUser(authentication);
        authService.deleteUser(user, deleteAccountRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "회원탈퇴되었습니다.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        // 쿠키에서 refreshToken 꺼내기
        String refreshToken = authService.getRefreshToken(request);

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh Token이 없습니다.");
        }

        String newAccessToken = authService.refreshAccessToken(refreshToken);

        if (newAccessToken.isBlank()) {
            return ResponseEntity.status(401).body("Refresh Token이 유효하지 않습니다.");
        }

        return ResponseEntity.ok(Map.of("token", newAccessToken));
    }
}
