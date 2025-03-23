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

    @Value("${jwt.secret.key}")
    private String salt;

    private final SecurityUserDetailsService securityUserDetailsService;
    private SecretKey secretKey;

    @PostConstruct
    protected void KeyInit() {
        secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * jwt 토큰 생성
     * @param user
     * @return
     */
    public final String createJwt(User user) {
        // 시간설정
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 24 * 60 * 60 * 1000; // 24시간
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .subject(user.getId().toString())
                .expiration(exp)
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
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

    public String getEmail(String token) {
        token = token.split(" ")[1].trim();
        Jws<Claims> claimsJwt = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

        return claimsJwt.getPayload().get("email", String.class);
    }

    public final Long getSubject(String token) {
        token = token.split(" ")[1].trim();
        Jws<Claims> claimsJwt = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        String subject = claimsJwt.getPayload().getSubject();
        return Long.valueOf(subject);
    }
}