package com.jyk.wordquiz.wordquiz.common.auth;

import com.jyk.wordquiz.wordquiz.model.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SecurityUserDetailsService securityUserDetailsService;

    // Must be at least 32 bytes for HMAC-SHA256
    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256";
    private static final long ACCESS_TOKEN_EXPIRE_MINUTES = 60L;
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7L;

    private SecretKey testSecretKey;

    @BeforeEach
    void setUp() {
        testSecretKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtTokenProvider, "salt", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRE", ACCESS_TOKEN_EXPIRE_MINUTES);
        ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRE", REFRESH_TOKEN_EXPIRE_DAYS);
    }

    // ─────────────────────────────────────────────────────────────────
    // Helper methods
    // ─────────────────────────────────────────────────────────────────

    private String buildRawAccessToken(String email) {
        return Jwts.builder()
                .subject("1")
                .claim("email", email)
                .claim("username", "testuser")
                .claim("type", JwtTokenProvider.TOKEN_TYPE_ACCESS)
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(testSecretKey)
                .compact();
    }

    private String buildRawRefreshToken() {
        return Jwts.builder()
                .subject("1")
                .claim("type", JwtTokenProvider.TOKEN_TYPE_REFRESH)
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                .signWith(testSecretKey)
                .compact();
    }

    private String buildExpiredAccessToken() {
        return Jwts.builder()
                .subject("1")
                .claim("email", "expired@example.com")
                .claim("type", JwtTokenProvider.TOKEN_TYPE_ACCESS)
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(testSecretKey)
                .compact();
    }

    private String buildRawAccessTokenWithoutTypeClaim() {
        return Jwts.builder()
                .subject("1")
                .claim("email", "test@example.com")
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(testSecretKey)
                .compact();
    }

    private User buildTestUser(String email) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setUsername("testuser");
        user.setPassword("password");
        return user;
    }

    // ─────────────────────────────────────────────────────────────────
    // validateToken() tests
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("null token returns false")
        void nullToken_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("valid access token with 'Bearer ' prefix returns true")
        void validAccessTokenWithBearerPrefix_returnsTrue() {
            String raw = buildRawAccessToken("user@example.com");
            assertThat(jwtTokenProvider.validateToken("Bearer " + raw)).isTrue();
        }

        @Test
        @DisplayName("valid access token without Bearer prefix (raw JWT) returns true")
        void validAccessTokenWithoutBearerPrefix_returnsTrue() {
            // extractRawToken() passes raw token through when no Bearer prefix found
            String raw = buildRawAccessToken("user@example.com");
            assertThat(jwtTokenProvider.validateToken(raw)).isTrue();
        }

        @Test
        @DisplayName("Bearer prefix matching is case-insensitive (lowercase 'bearer ')")
        void bearerPrefixCaseInsensitive_lowercase_returnsTrue() {
            String raw = buildRawAccessToken("user@example.com");
            assertThat(jwtTokenProvider.validateToken("bearer " + raw)).isTrue();
        }

        @Test
        @DisplayName("Bearer prefix matching is case-insensitive (uppercase 'BEARER ')")
        void bearerPrefixCaseInsensitive_uppercase_returnsTrue() {
            String raw = buildRawAccessToken("user@example.com");
            assertThat(jwtTokenProvider.validateToken("BEARER " + raw)).isTrue();
        }

        @Test
        @DisplayName("Bearer prefix matching is case-insensitive (mixed case 'BeArEr ')")
        void bearerPrefixCaseInsensitive_mixedCase_returnsTrue() {
            String raw = buildRawAccessToken("user@example.com");
            assertThat(jwtTokenProvider.validateToken("BeArEr " + raw)).isTrue();
        }

        @Test
        @DisplayName("refresh token with 'Bearer ' prefix returns false (type mismatch)")
        void refreshTokenWithBearerPrefix_returnsFalse() {
            String raw = buildRawRefreshToken();
            assertThat(jwtTokenProvider.validateToken("Bearer " + raw)).isFalse();
        }

        @Test
        @DisplayName("expired access token returns false")
        void expiredAccessToken_returnsFalse() {
            String raw = buildExpiredAccessToken();
            assertThat(jwtTokenProvider.validateToken("Bearer " + raw)).isFalse();
        }

        @Test
        @DisplayName("token with invalid signature returns false")
        void invalidSignature_returnsFalse() {
            SecretKey otherKey = Keys.hmacShaKeyFor(
                    "other-secret-key-that-is-long-enough-for-hmac".getBytes(StandardCharsets.UTF_8)
            );
            String tamperedToken = Jwts.builder()
                    .subject("1")
                    .claim("type", JwtTokenProvider.TOKEN_TYPE_ACCESS)
                    .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                    .signWith(otherKey)
                    .compact();
            assertThat(jwtTokenProvider.validateToken("Bearer " + tamperedToken)).isFalse();
        }

        @Test
        @DisplayName("completely garbage string returns false")
        void garbageString_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("not.a.jwt.token")).isFalse();
        }

        @Test
        @DisplayName("'Bearer ' with no raw token returns false (empty after prefix)")
        void bearerPrefixOnly_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("Bearer ")).isFalse();
        }

        @Test
        @DisplayName("empty string returns false")
        void emptyString_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("whitespace-only string returns false")
        void whitespaceOnly_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("   ")).isFalse();
        }

        @Test
        @DisplayName("access token without type claim returns false")
        void accessTokenWithoutTypeClaim_returnsFalse() {
            String raw = buildRawAccessTokenWithoutTypeClaim();
            assertThat(jwtTokenProvider.validateToken("Bearer " + raw)).isFalse();
        }

        @Test
        @DisplayName("'Bearer' without trailing space (no space separator) is treated as raw token — invalid JWT returns false")
        void bearerWithoutTrailingSpace_returnsFalse() {
            // "Bearerxxx" has no space after Bearer so extractRawToken treats the whole
            // string as the raw JWT (no Bearer prefix), which is not a valid JWT.
            assertThat(jwtTokenProvider.validateToken("BearerInvalidPayload")).isFalse();
        }

        @Test
        @DisplayName("leading/trailing whitespace around the full token is handled")
        void tokenWithLeadingTrailingWhitespace_returnsTrue() {
            String raw = buildRawAccessToken("user@example.com");
            // extractRawToken trims the full string first
            assertThat(jwtTokenProvider.validateToken("  Bearer " + raw + "  ")).isTrue();
        }

        @Test
        @DisplayName("'Bearer  token' (extra space after Bearer) is valid — trimmed to raw JWT")
        void extraSpaceAfterBearer_returnsTrue() {
            String raw = buildRawAccessToken("user@example.com");
            // substring(7) gives " <raw>", then .trim() produces raw
            assertThat(jwtTokenProvider.validateToken("Bearer  " + raw)).isTrue();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // getEmail() tests
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getEmail()")
    class GetEmail {

        @Test
        @DisplayName("valid access token with 'Bearer ' prefix returns correct email")
        void validTokenWithBearerPrefix_returnsEmail() {
            String email = "user@example.com";
            String raw = buildRawAccessToken(email);
            assertThat(jwtTokenProvider.getEmail("Bearer " + raw)).isEqualTo(email);
        }

        @Test
        @DisplayName("valid access token without Bearer prefix (raw JWT) returns email")
        void validRawToken_returnsEmail() {
            String email = "user@example.com";
            String raw = buildRawAccessToken(email);
            assertThat(jwtTokenProvider.getEmail(raw)).isEqualTo(email);
        }

        @Test
        @DisplayName("Bearer prefix matching is case-insensitive for getEmail")
        void bearerCaseInsensitive_returnsEmail() {
            String email = "user@example.com";
            String raw = buildRawAccessToken(email);
            assertThat(jwtTokenProvider.getEmail("BEARER " + raw)).isEqualTo(email);
        }

        @Test
        @DisplayName("null token throws IllegalArgumentException")
        void nullToken_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> jwtTokenProvider.getEmail(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("token is required");
        }

        @Test
        @DisplayName("'Bearer ' with no raw token throws IllegalArgumentException")
        void bearerPrefixOnly_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> jwtTokenProvider.getEmail("Bearer "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("invalid bearer token");
        }

        @Test
        @DisplayName("empty string throws IllegalArgumentException")
        void emptyString_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> jwtTokenProvider.getEmail(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("token with multiple spaces (invalid format) throws IllegalArgumentException")
        void tokenWithMultipleSpaces_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> jwtTokenProvider.getEmail("Bearer part1 part2"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("invalid bearer token");
        }

        @Test
        @DisplayName("token with no email claim returns null")
        void tokenWithNoEmailClaim_returnsNull() {
            String rawNoEmail = Jwts.builder()
                    .subject("1")
                    .claim("type", JwtTokenProvider.TOKEN_TYPE_ACCESS)
                    .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                    .signWith(testSecretKey)
                    .compact();
            assertThat(jwtTokenProvider.getEmail("Bearer " + rawNoEmail)).isNull();
        }

        @Test
        @DisplayName("createAccessToken() produces a token whose email is retrievable via getEmail()")
        void roundTrip_createAccessToken_getEmail() {
            String email = "roundtrip@example.com";
            User user = buildTestUser(email);
            String accessToken = jwtTokenProvider.createAccessToken(user);
            // createAccessToken returns a raw JWT (no Bearer prefix)
            assertThat(jwtTokenProvider.getEmail(accessToken)).isEqualTo(email);
        }

        @Test
        @DisplayName("createAccessToken() token prefixed with 'Bearer ' is accepted by getEmail()")
        void roundTrip_createAccessToken_withBearerPrefix_getEmail() {
            String email = "bearer-roundtrip@example.com";
            User user = buildTestUser(email);
            String accessToken = jwtTokenProvider.createAccessToken(user);
            assertThat(jwtTokenProvider.getEmail("Bearer " + accessToken)).isEqualTo(email);
        }
    }
}
