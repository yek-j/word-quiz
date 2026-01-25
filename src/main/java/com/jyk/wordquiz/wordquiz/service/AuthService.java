package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
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

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider provider;

    public AuthService(RefreshTokenService refreshTokenService, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider provider) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.provider = provider;
    }

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

        String refreshToken = provider.createRefreshToken(user.getId());
        refreshTokenService.refreshTokenSave(user.getId(), refreshToken);

        return LoginResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(provider.createAccessToken(user))
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 사용자 정보
     * @param user: 사용자
     * @return UserInfoResponse: 사용자 정보
     */
    public UserInfoResponse getUserInfo(User user){
        return new UserInfoResponse(user.getUsername(), user.getEmail());
    }

    /**
     * 사용자 정보 수정
     * @param user: 사용자
     * @param userInfoReq: 수정할 사용자 정보
     */
    @Transactional
    public void updateUserInfo(User user, UserInfoRequest userInfoReq) {
        
        if(!userInfoReq.getUsername().isBlank()) {
            user.setUsername(userInfoReq.getUsername());
        }
        
        user.setUsername(userInfoReq.getUsername());

        userRepository.save(user);
    }

    /**
     * 비밀번호 변경
     * @param user: 사용자
     * @param changePwd: 현재 비밀번호와 변경할 비밀번호
     */
    @Transactional
    public void changePassword(User user, ChangePwd changePwd){
        
        if(!passwordEncoder.matches(changePwd.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(changePwd.getNewPassword()));

        userRepository.save(user);
    }

    /**
     * 사용자 탈퇴
     * @param user: 사용자
     * @param deleteReq: 비밀번호 정보
     */
    @Transactional
    public void deleteUser(User user, DeleteAccountRequest deleteReq) {
        
        if(!passwordEncoder.matches(deleteReq.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }

    /**
     * refresh token 검증 후 access token 발급
     * @param refreshToken: 토큰
     * @return new access token
     */
    public String refreshAccessToken(String refreshToken) {
        // 유효기간 확인
        if(!provider.validateRefreshToken(refreshToken)) return "";

        // userId 가져오기
        Long userId = provider.getSubject(refreshToken);

        String redisRefreshToken = refreshTokenService.findRefreshToken(userId);

        if (refreshToken.equals(redisRefreshToken)) {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return provider.createAccessToken(user.get());
            }
        }

        return "";
    }
}
