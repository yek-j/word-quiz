package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.dto.request.*;
import com.jyk.wordquiz.wordquiz.model.dto.response.LoginResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.UserInfoResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> siginUp(@RequestBody SignupRequest signupReq) {
        authService.siginup(signupReq);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "회원가입이 성공적으로 완료되었습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", loginResponse);
        return ResponseEntity.ok(response);
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

}
