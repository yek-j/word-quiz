package com.jyk.wordquiz.wordquiz.controller;

import com.jyk.wordquiz.wordquiz.common.auth.AuthUtil;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<?> getUsers(Authentication authentication,
                                      @RequestParam String username) {
        User user = AuthUtil.getCurrentUser(authentication);

        List<String> result = userService.getUserList(user, username);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "사용자 이름 친구 검색 결과입니다.");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

}
