package com.jyk.wordquiz.wordquiz.common.auth;

import com.jyk.wordquiz.wordquiz.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_TYPE = "type";

    @Value("${jwt.access-token-expire-minutes}")
    private long ACCESS_TOKEN_EXPIRE;
    @Value("${jwt.refresh-token-expire-days}")
    private long REFRESH_TOKEN_EXPIRE; // 7일

    @Value("${jwt.secret.key}")
    private String salt;

    private final SecurityUserDetailsService securityUserDetailsService;
    private SecretKey secretKey;

    @PostConstruct
    protected void KeyInit() {
        secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * AccessToken(JWT) 토큰 생성
     * @param user
     * @return
     */
    public final String createAccessToken(User user) {
        long nowMillis = System.currentTimeMillis();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .expiration(new Date(nowMillis + ACCESS_TOKEN_EXPIRE * 60 * 1000))
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .claim(CLAIM_TYPE, TOKEN_TYPE_ACCESS)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     * @param userId: 사용자 ID
     * @return refreshToken
     */
    public String createRefreshToken(Long userId) {
        long nowMillis = System.currentTimeMillis();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .expiration(new Date(nowMillis + REFRESH_TOKEN_EXPIRE * 24 * 60 * 60 * 1000))
                .claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername(this.getEmail(token));
        if(userDetails == null) return null;
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public String getToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    /**
     * Access Token 검증.
     * 1) Bearer prefix 확인
     * 2) 서명/만료 검증
     * 3) type=access 검증 (refresh 토큰을 access로 사용하는 것을 차단)
     */
    public boolean validateToken(String token) {
        if(token == null) return false;

        try {
            // Bearer
            if (token.length() < "BEARER ".length()
                || !token.substring(0, "BEARER ".length()).equalsIgnoreCase("BEARER ")) {
                return false;
            }
            String raw = token.split(" ")[1].trim();
            Jws<Claims> claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(raw);

            Claims payload = claims.getPayload();
            if (payload.getExpiration().before(new Date())) return false;
            return TOKEN_TYPE_ACCESS.equals(payload.get(CLAIM_TYPE, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh Token 검증. type=refresh 검증 포함.
     */
    public boolean validateRefreshToken(String token) {
        if (token == null) return false;
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey).build()
                    .parseSignedClaims(token);
            Claims payload = claims.getPayload();
            if (payload.getExpiration().before(new Date())) return false;
            return TOKEN_TYPE_REFRESH.equals(payload.get(CLAIM_TYPE, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmail(String token) {
        token = token.split(" ")[1].trim();
        Jws<Claims> claimsJwt = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

        return claimsJwt.getPayload().get("email", String.class);
    }

    public final Long getSubject(String token) {
        Jws<Claims> claimsJwt = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        String subject = claimsJwt.getPayload().getSubject();
        return Long.valueOf(subject);
    }

    /**
     * 'Bearer xxx' 형식 또는 raw 토큰에서 jti(고유 식별자)를 추출한다.
     * 블랙리스트 등록/조회에 사용.
     */
    public String getJti(String token) {
        String raw = token.contains(" ") ? token.split(" ")[1].trim() : token;
        Jws<Claims> claimsJwt = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(raw);
        return claimsJwt.getPayload().getId();
    }

    /**
     * 토큰 만료 시점(epoch millis)을 반환한다. 블랙리스트 TTL 계산에 사용.
     */
    public long getExpirationMillis(String token) {
        String raw = token.contains(" ") ? token.split(" ")[1].trim() : token;
        Jws<Claims> claimsJwt = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(raw);
        return claimsJwt.getPayload().getExpiration().getTime();
    }
}
