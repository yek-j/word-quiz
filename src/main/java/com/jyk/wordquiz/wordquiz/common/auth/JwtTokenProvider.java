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
                .subject(user.getId().toString())
                .expiration(new Date(nowMillis + ACCESS_TOKEN_EXPIRE * 60 * 1000))
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
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
                .subject(userId.toString())
                .expiration(new Date(nowMillis + REFRESH_TOKEN_EXPIRE * 24 * 60 * 60 * 1000))
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

    public boolean validateToken(String token) {
        if(token == null) return false;

        try {
            // Bearer
            if (!token.substring(0, "BEARER ".length()).equalsIgnoreCase("BEARER ")) {
                return false;
            } else {
                token = token.split(" ")[1].trim();
            }
            Jws<Claims> claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

            return !claims.getPayload().getExpiration().before(new Date()); // 현재 시간보다 지났다면 만료
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey).build()
                    .parseSignedClaims(token);
            return !claims.getPayload().getExpiration().before(new Date());
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
}