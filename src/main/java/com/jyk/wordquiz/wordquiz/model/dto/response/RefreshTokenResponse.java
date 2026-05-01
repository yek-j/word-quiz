package com.jyk.wordquiz.wordquiz.model.dto.response;

/**
 * /auth/refresh 호출 결과. accessToken은 응답 본문, refreshToken은 컨트롤러에서
 * HttpOnly 쿠키로 다시 내려준다 (Refresh Token Rotation).
 */
public record RefreshTokenResponse(String accessToken, String refreshToken) {
}
