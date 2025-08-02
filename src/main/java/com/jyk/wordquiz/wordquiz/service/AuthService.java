package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.DuplicateUserException;
import com.jyk.wordquiz.wordquiz.model.dto.request.*;
import com.jyk.wordquiz.wordquiz.model.dto.response.LoginResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.UserInfoResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider provider;

    /**
     * 회원가입
     * @param signupReq: 회원가입 정보
     */
    @Transactional
    public void siginup(SignupRequest signupReq) {
        Optional<User> findUser = userRepository.findByEmail(signupReq.getEmail());

        if(findUser.isPresent()) {
            throw new DuplicateUserException("사용할 수 없는 이메일입니다.");
        } else {
            User newUser = new User();
            newUser.setEmail(signupReq.getEmail());
            newUser.setUsername(signupReq.getUsername());
            newUser.setPassword(passwordEncoder.encode(signupReq.getPassword()));
            userRepository.save(newUser);
        }
    }

    /**
     * 로그인
     * @param loginReq: 로그인 정보
     * @return LoginResponse: 사용자 정보와 토큰
     */
    public LoginResponse login(LoginRequest loginReq){
        Optional<User> findUser = userRepository.findByEmail(loginReq.getEmail());

        if(findUser.isEmpty()) {
            throw new BadCredentialsException("로그인에 실패했습니다.");
        }

        User user = findUser.get();
        if(!passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("로그인에 실패했습니다.");
        }

        return LoginResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .token(provider.createJwt(user))
                .build();
    }

    /**
     * 사용자 정보
     * @param token: jwtToken
     * @return UserInfoResponse: 사용자 정보
     */
    public UserInfoResponse getUserInfo(String token){
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));
        return new UserInfoResponse(user.getUsername(), user.getEmail());
    }

    /**
     * 사용자 정보 수정
     * @param token: jwtToken
     * @param userInfoReq: 수정할 사용자 정보
     */
    public void updateUserInfo(String token, UserInfoRequest userInfoReq) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        if(!userInfoReq.getUsername().isBlank()) {
            user.setUsername(userInfoReq.getUsername());
        }
    }

    /**
     * 비밀번호 변경
     * @param token: jwtToken
     * @param changePwd: 현재 비밀번호와 변경할 비밀번호
     */
    @Transactional
    public void changePassword(String token, ChangePwd changePwd){
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        if(!passwordEncoder.matches(changePwd.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(changePwd.getNewPassword()));

        userRepository.save(user);
    }

    /**
     * 사용자 탈퇴
     * @param token: jwtToken
     * @param deleteReq: 비밀번호 정보
     */
    @Transactional
    public void deleteUser(String token, DeleteAccountRequest deleteReq) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        if(!passwordEncoder.matches(deleteReq.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }
}
