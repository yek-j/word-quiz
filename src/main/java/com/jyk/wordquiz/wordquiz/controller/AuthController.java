package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.*;
import com.jyk.wordquiz.wordquiz.model.dto.response.ApiResponseWrapper;
import com.jyk.wordquiz.wordquiz.model.dto.response.LoginResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.RefreshTokenResponse;
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

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseWrapper.success("회원가입이 성공적으로 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody LoginRequest loginRequest,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        LoginResponse loginResponse = authService.login(loginRequest, ip, userAgent);

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

        return ResponseEntity.ok(ApiResponseWrapper.success("로그인 성공", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        User user = AuthUtil.getCurrentUser(authentication);

        // redis에서 refreshToken 삭제 + 현재 access token jti를 블랙리스트에 등록
        String accessTokenHeader = request.getHeader("Authorization");
        authService.logout(user.getId(), accessTokenHeader);

        // 쿠키 만료 시키기
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(ApiResponseWrapper.success("로그아웃 완료"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> userInfo(Authentication authentication) {
        User user = AuthUtil.getCurrentUser(authentication);
        UserInfoResponse userInfo = authService.getUserInfo(user);

        return ResponseEntity.ok(ApiResponseWrapper.success("사용자 정보를 가져왔습니다.", userInfo));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(Authentication authentication, @RequestBody UserInfoRequest userInfoRequest) {
        User user = AuthUtil.getCurrentUser(authentication);

        authService.updateUserInfo(user, userInfoRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("사용자 정보가 성공적으로 변경되었습니다."));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePwd changePwd) {
        User user = AuthUtil.getCurrentUser(authentication);
        authService.changePassword(user, changePwd);

        return ResponseEntity.ok(ApiResponseWrapper.success("비밀번호가 성공적으로 변경되었습니다."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(@RequestBody @Valid DeleteAccountRequest deleteAccountRequest, Authentication authentication) {
        User user = AuthUtil.getCurrentUser(authentication);
        authService.deleteUser(user, deleteAccountRequest);

        return ResponseEntity.ok(ApiResponseWrapper.success("회원탈퇴되었습니다."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request,
                                          HttpServletResponse response) {
        // 쿠키에서 refreshToken 꺼내기
        String refreshToken = authService.getRefreshToken(request);

        if (refreshToken == null) {
            return ResponseEntity.status(401).body(ApiResponseWrapper.fail("Refresh Token이 없습니다."));
        }

        // Refresh Token Rotation: 새 access + 새 refresh 발급
        RefreshTokenResponse rotated = authService.refreshAccessToken(refreshToken);

        if (rotated == null) {
            return ResponseEntity.status(401).body(ApiResponseWrapper.fail("Refresh Token이 유효하지 않습니다."));
        }

        // 새 refresh token을 HttpOnly 쿠키로 다시 내려준다
        ResponseCookie cookie = ResponseCookie.from("refreshToken", rotated.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(ApiResponseWrapper.success("토큰 갱신 성공", Map.of("token", rotated.accessToken())));
    }
}
