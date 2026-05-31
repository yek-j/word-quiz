package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.DuplicateUserException;
import com.jyk.wordquiz.wordquiz.model.dto.request.*;
import com.jyk.wordquiz.wordquiz.model.dto.response.LoginResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.RefreshTokenResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.UserInfoResponse;
import com.jyk.wordquiz.wordquiz.model.entity.LoginLog;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.repository.LoginLogRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider provider;

    public AuthService(RefreshTokenService refreshTokenService,
                       TokenBlacklistService tokenBlacklistService,
                       UserRepository userRepository,
                       LoginLogRepository loginLogRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider provider) {
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
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
     * @param ip: IP
     * @param userAgent: UserAgent
     * @return: 로그인 Response
     */
    @Transactional
    public LoginResponse login(LoginRequest loginReq, String ip, String userAgent){
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

        // 로그인 로그 추가
        LoginLog loginLog = LoginLog.builder()
                .userId(user.getId())
                .userAgent(userAgent)
                .userClientIp(ip)
                .build();

        loginLogRepository.save(loginLog);

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
     * Refresh Token Rotation: 검증 후 새 access + 새 refresh를 함께 발급한다.
     * 검증 실패 또는 Redis의 토큰과 불일치 시 null 반환.
     * @param refreshToken: 쿠키에서 꺼낸 refresh token
     * @return 새 access/refresh 페어 (실패 시 null)
     */
    public RefreshTokenResponse refreshAccessToken(String refreshToken) {
        // 유효기간 + type=refresh 확인
        if(!provider.validateRefreshToken(refreshToken)) return null;

        Long userId = provider.getSubject(refreshToken);
        String redisRefreshToken = refreshTokenService.findRefreshToken(userId);

        // 저장된 refresh와 동일해야 함 (재사용/탈취 방지)
        if (!refreshToken.equals(redisRefreshToken)) return null;

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return null;

        String newAccess = provider.createAccessToken(user.get());
        String newRefresh = provider.createRefreshToken(userId);

        // 기존 refresh 폐기 + 새 refresh 저장 (TTL 갱신)
        refreshTokenService.refreshTokenSave(userId, newRefresh);

        return new RefreshTokenResponse(newAccess, newRefresh);
    }

    /**
     * 로그아웃 처리.
     * - Redis의 refresh token 삭제
     * - 현재 access token이 있다면 jti를 블랙리스트에 등록(만료시각까지 TTL)
     * @param userId: 사용자 ID
     * @param accessTokenHeader: 'Bearer xxx' 형식 또는 raw access token (nullable)
     */
    public void logout(Long userId, String accessTokenHeader) {
        refreshTokenService.deleteRefreshToken(userId);

        if (accessTokenHeader == null) return;
        try {
            String jti = provider.getJti(accessTokenHeader);
            long expMillis = provider.getExpirationMillis(accessTokenHeader);
            long ttl = expMillis - System.currentTimeMillis();
            tokenBlacklistService.blacklist(jti, ttl);
        } catch (Exception ignored) {
            // 토큰 파싱 실패 시 블랙리스트 등록 생략 (이미 무효한 토큰)
        }
    }

    /**
     * Cookie에서 RefreshToken 가져오기
     * @param request: HttpServletRequest
     * @return Refresh Token
     */
    public String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        return refreshToken;
    }

    /**
     * Redis에서 Refresh Token 삭제
     * @param userId: 사용자 ID
     */
    public void deleteRefreshToken(Long userId) {
        refreshTokenService.deleteRefreshToken(userId);
    }

}
