package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.exception.DuplicateUserException;
import com.jyk.wordquiz.wordquiz.model.dto.request.ChangePwd;
import com.jyk.wordquiz.wordquiz.model.dto.request.DeleteAccountRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.LoginRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.SignupRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.LoginResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.UserInfoResponse;
import com.jyk.wordquiz.wordquiz.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
        String jwtToken = authentication.getCredentials().toString();
        UserInfoResponse userInfo = authService.getUserInfo(jwtToken);

        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePwd changePwd) {
        String jwtToken = authentication.getCredentials().toString();
        authService.changePassword(jwtToken, changePwd);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "비밀번호가 성공적으로 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(@RequestBody @Valid DeleteAccountRequest deleteAccountRequest, Authentication authentication) {
        String jwtToken = authentication.getCredentials().toString();
        authService.deleteUser(jwtToken, deleteAccountRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "회원탈퇴되었습니다.");

        return ResponseEntity.ok(response);
    }

}
