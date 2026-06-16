package com.jyk.wordquiz.wordquiz.common.auth;

import com.jyk.wordquiz.wordquiz.model.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for changes introduced in PR:
 * - validateToken(): Bearer prefix removal and format validation delegated to extractRawToken()
 * - getEmail(): token parsing delegated to extractRawToken()
 *
 * extractRawToken() is private; its behavior is verified indirectly through these two methods.
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTests {

    private static final String SECRET =
            "test-secret-key-for-jwt-unit-tests-must-be-at-least-32-bytes";

    @Mock
    private SecurityUserDetailsService securityUserDetailsService;

    private JwtTokenProvider jwtTokenProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenProvider = new JwtTokenProvider(securityUserDetailsService);
        ReflectionTestUtils.setField(jwtTokenProvider, "salt", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRE", 60L);
        ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRE", 7L);
        // Trigger @PostConstruct initialization
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "KeyInit");

        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User buildUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUsername("testuser");
        user.setPassword("password");
        return user;
    }

    /** Builds a raw (no Bearer prefix) access token signed with the test key. */
    private String buildRawAccessToken(User user) {
        return jwtTokenProvider.createAccessToken(user);
    }

    /** Builds a raw (no Bearer prefix) refresh token signed with the test key. */
    private String buildRawRefreshToken(Long userId) {
        return jwtTokenProvider.createRefreshToken(userId);
    }

    /** Builds an already-expired raw access token for testing expiry paths. */
    private String buildExpiredRawAccessToken(User user) {
        long past = System.currentTimeMillis() - 60_000L;
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .expiration(new Date(past))
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .claim("type", "access")
                .signWith(secretKey)
                .compact();
    }

    /** Builds a raw access token signed with a different (wrong) key. */
    private String buildWrongKeyRawAccessToken(User user) {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "wrong-secret-key-for-testing-signature-failure-xx".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .claim("email", user.getEmail())
                .claim("type", "access")
                .signWith(wrongKey)
                .compact();
    }

    // =========================================================================
    // validateToken() tests
    // =========================================================================

    @Nested
    @DisplayName("validateToken()")
    class ValidateTokenTests {

        @Test
        @DisplayName("valid Bearer-prefixed access token returns true")
        void validBearerAccessToken_returnsTrue() {
            User user = buildUser(1L, "user@example.com");
            String raw = buildRawAccessToken(user);

            assertThat(jwtTokenProvider.validateToken("Bearer " + raw)).isTrue();
        }

        @Test
        @DisplayName("valid raw access token (no Bearer prefix) returns true")
        void validRawAccessToken_returnsTrue() {
            User user = buildUser(1L, "user@example.com");
            String raw = buildRawAccessToken(user);

            assertThat(jwtTokenProvider.validateToken(raw)).isTrue();
        }

        @Test
        @DisplayName("Bearer prefix is case-insensitive (BEARER, bearer)")
        void bearerPrefixIsCaseInsensitive_returnsTrue() {
            User user = buildUser(1L, "user@example.com");
            String raw = buildRawAccessToken(user);

            assertThat(jwtTokenProvider.validateToken("BEARER " + raw)).isTrue();
            assertThat(jwtTokenProvider.validateToken("bearer " + raw)).isTrue();
        }

        @Test
        @DisplayName("token with leading/trailing spaces is accepted after trimming")
        void tokenWithOuterSpaces_returnsTrue() {
            User user = buildUser(1L, "user@example.com");
            String raw = buildRawAccessToken(user);

            assertThat(jwtTokenProvider.validateToken("  Bearer " + raw + "  ")).isTrue();
        }

        @Test
        @DisplayName("null token returns false")
        void nullToken_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("token that is only 'Bearer ' (empty payload) returns false")
        void bearerPrefixWithEmptyPayload_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("Bearer ")).isFalse();
        }

        @Test
        @DisplayName("token with spaces inside the JWT part returns false")
        void tokenWithInternalSpaces_returnsFalse() {
            // A token value that contains a space after stripping Bearer prefix is invalid
            assertThat(jwtTokenProvider.validateToken("Bearer part1 part2")).isFalse();
        }

        @Test
        @DisplayName("empty string token returns false")
        void emptyStringToken_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("entirely whitespace token returns false")
        void whitespaceOnlyToken_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("   ")).isFalse();
        }

        @Test
        @DisplayName("Bearer-prefixed refresh token returns false (type check)")
        void bearerRefreshToken_returnsFalse() {
            String refreshRaw = buildRawRefreshToken(1L);

            assertThat(jwtTokenProvider.validateToken("Bearer " + refreshRaw)).isFalse();
        }

        @Test
        @DisplayName("raw refresh token (no Bearer) returns false (type check)")
        void rawRefreshToken_returnsFalse() {
            String refreshRaw = buildRawRefreshToken(1L);

            assertThat(jwtTokenProvider.validateToken(refreshRaw)).isFalse();
        }

        @Test
        @DisplayName("expired access token returns false")
        void expiredAccessToken_returnsFalse() {
            User user = buildUser(1L, "user@example.com");
            String expiredRaw = buildExpiredRawAccessToken(user);

            assertThat(jwtTokenProvider.validateToken("Bearer " + expiredRaw)).isFalse();
        }

        @Test
        @DisplayName("token signed with wrong key returns false")
        void wrongKeyToken_returnsFalse() {
            User user = buildUser(1L, "user@example.com");
            String wrongKeyToken = buildWrongKeyRawAccessToken(user);

            assertThat(jwtTokenProvider.validateToken("Bearer " + wrongKeyToken)).isFalse();
        }

        @Test
        @DisplayName("malformed / arbitrary string returns false")
        void malformedToken_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("Bearer not.a.real.jwt")).isFalse();
            assertThat(jwtTokenProvider.validateToken("this-is-not-a-jwt")).isFalse();
        }

        @Test
        @DisplayName("regression: raw access token that previously required split(' ')[1] still validates")
        void regressionRawTokenNoBearerStillValid() {
            // Before the refactor getEmail() used split(" ")[1], which would throw on a raw JWT.
            // validateToken() previously required 'BEARER ' prefix; now raw tokens are also accepted.
            User user = buildUser(42L, "regression@example.com");
            String raw = buildRawAccessToken(user);

            // raw token must pass validation (new behaviour)
            assertThat(jwtTokenProvider.validateToken(raw)).isTrue();
        }
    }

    // =========================================================================
    // getEmail() tests
    // =========================================================================

    @Nested
    @DisplayName("getEmail()")
    class GetEmailTests {

        @Test
        @DisplayName("returns email from Bearer-prefixed access token")
        void bearerAccessToken_returnsEmail() {
            User user = buildUser(1L, "alice@example.com");
            String raw = buildRawAccessToken(user);

            String email = jwtTokenProvider.getEmail("Bearer " + raw);

            assertThat(email).isEqualTo("alice@example.com");
        }

        @Test
        @DisplayName("returns email from raw access token (no Bearer prefix)")
        void rawAccessToken_returnsEmail() {
            User user = buildUser(1L, "bob@example.com");
            String raw = buildRawAccessToken(user);

            String email = jwtTokenProvider.getEmail(raw);

            assertThat(email).isEqualTo("bob@example.com");
        }

        @Test
        @DisplayName("Bearer prefix is case-insensitive for getEmail()")
        void bearerCaseInsensitive_returnsEmail() {
            User user = buildUser(1L, "carol@example.com");
            String raw = buildRawAccessToken(user);

            assertThat(jwtTokenProvider.getEmail("BEARER " + raw)).isEqualTo("carol@example.com");
            assertThat(jwtTokenProvider.getEmail("bearer " + raw)).isEqualTo("carol@example.com");
        }

        @Test
        @DisplayName("null token throws IllegalArgumentException")
        void nullToken_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> jwtTokenProvider.getEmail(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("token is required");
        }

        @Test
        @DisplayName("'Bearer ' with empty payload throws IllegalArgumentException")
        void bearerWithEmptyPayload_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> jwtTokenProvider.getEmail("Bearer "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("invalid bearer token");
        }

        @Test
        @DisplayName("token with internal spaces throws IllegalArgumentException")
        void tokenWithInternalSpaces_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> jwtTokenProvider.getEmail("Bearer part1 part2"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("invalid bearer token");
        }

        @Test
        @DisplayName("regression: raw token no longer requires Bearer prefix for getEmail()")
        void regressionRawTokenNoBearer_returnsEmail() {
            // Before the refactor getEmail() called token.split(" ")[1], meaning a raw JWT
            // (without a space) would cause an ArrayIndexOutOfBoundsException.
            // After the refactor, raw tokens are valid and email is extracted correctly.
            User user = buildUser(7L, "regression@example.com");
            String raw = buildRawAccessToken(user);

            String email = jwtTokenProvider.getEmail(raw);

            assertThat(email).isEqualTo("regression@example.com");
        }

        @Test
        @DisplayName("token with leading/trailing spaces still extracts correct email")
        void tokenWithOuterSpaces_returnsEmail() {
            User user = buildUser(1L, "dave@example.com");
            String raw = buildRawAccessToken(user);

            String email = jwtTokenProvider.getEmail("  Bearer " + raw + "  ");

            assertThat(email).isEqualTo("dave@example.com");
        }
    }
}