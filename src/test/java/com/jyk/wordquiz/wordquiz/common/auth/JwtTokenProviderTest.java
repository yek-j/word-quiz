package com.jyk.wordquiz.wordquiz.common.auth;

import com.jyk.wordquiz.wordquiz.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtTokenProvider} focusing on the PR changes:
 * - {@code validateToken}: now delegates Bearer-prefix stripping to {@code extractRawToken}
 * - {@code getEmail}: now delegates Bearer-prefix stripping to {@code extractRawToken}
 *
 * The private {@code extractRawToken} method is tested indirectly through these public APIs.
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    // Must be at least 256 bits (32 bytes) for HMAC-SHA256
    private static final String TEST_SECRET =
            "test-secret-key-that-is-long-enough-for-hmac-sha256";

    @Mock
    private SecurityUserDetailsService securityUserDetailsService;

    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String rawAccessToken;
    private String bearerAccessToken;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(securityUserDetailsService);

        ReflectionTestUtils.setField(jwtTokenProvider, "salt", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRE", 30L);   // 30 minutes
        ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRE", 7L);   // 7 days

        // Initialize secretKey via @PostConstruct method
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "KeyInit");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("password");

        rawAccessToken = jwtTokenProvider.createAccessToken(testUser);
        bearerAccessToken = "Bearer " + rawAccessToken;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateToken — tests covering extractRawToken behaviour indirectly
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("validateToken returns false when token is null")
    void validateToken_withNull_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns true for a valid 'Bearer <access-token>' string")
    void validateToken_withValidBearerPrefixedAccessToken_returnsTrue() {
        assertThat(jwtTokenProvider.validateToken(bearerAccessToken)).isTrue();
    }

    @Test
    @DisplayName("validateToken accepts 'bearer ' (all lowercase) prefix — extractRawToken is case-insensitive")
    void validateToken_withLowercaseBearerPrefix_returnsTrue() {
        String lowercaseBearer = "bearer " + rawAccessToken;
        assertThat(jwtTokenProvider.validateToken(lowercaseBearer)).isTrue();
    }

    @Test
    @DisplayName("validateToken accepts 'BEARER ' (all uppercase) prefix — extractRawToken is case-insensitive")
    void validateToken_withUppercaseBearerPrefix_returnsTrue() {
        String uppercaseBearer = "BEARER " + rawAccessToken;
        assertThat(jwtTokenProvider.validateToken(uppercaseBearer)).isTrue();
    }

    @Test
    @DisplayName("validateToken trims leading/trailing whitespace from the full token string")
    void validateToken_withLeadingAndTrailingSpaces_returnsTrue() {
        String spacePadded = "  Bearer " + rawAccessToken + "  ";
        assertThat(jwtTokenProvider.validateToken(spacePadded)).isTrue();
    }

    @Test
    @DisplayName("validateToken accepts a raw JWT (no Bearer prefix) — extractRawToken passes it through")
    void validateToken_withRawTokenNoBearerPrefix_returnsTrue() {
        assertThat(jwtTokenProvider.validateToken(rawAccessToken)).isTrue();
    }

    @Test
    @DisplayName("validateToken returns false for an empty string")
    void validateToken_withEmptyString_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false when token is only whitespace")
    void validateToken_withOnlyWhitespace_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("   ")).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false when 'Bearer ' is present but JWT part is missing")
    void validateToken_withBearerPrefixAndEmptyJwtPart_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("Bearer ")).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false when raw part after Bearer contains a space (invalid format)")
    void validateToken_withBearerAndJwtContainingInternalSpace_returnsFalse() {
        // A space inside the raw token part is rejected by extractRawToken
        String tokenWithSpace = "Bearer " + rawAccessToken + " extra";
        assertThat(jwtTokenProvider.validateToken(tokenWithSpace)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for a refresh token (type=refresh is not accepted)")
    void validateToken_withRefreshToken_returnsFalse() {
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId());
        // Note: validateToken checks type=access; refresh tokens have type=refresh
        assertThat(jwtTokenProvider.validateToken("Bearer " + refreshToken)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false when the JWT signature has been tampered with")
    void validateToken_withTamperedSignature_returnsFalse() {
        // Replace the last few characters of the signature portion
        String tampered = bearerAccessToken.substring(0, bearerAccessToken.length() - 4) + "XXXX";
        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for a completely random string")
    void validateToken_withRandomString_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for 'Bearer' without trailing space (no separator)")
    void validateToken_withBearerWordOnlyNoSpace_returnsFalse() {
        // "Bearer" alone is not a valid JWT
        assertThat(jwtTokenProvider.validateToken("Bearer")).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEmail — tests covering extractRawToken behaviour indirectly
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getEmail extracts the correct email from a 'Bearer <access-token>' string")
    void getEmail_withValidBearerPrefixedToken_returnsEmail() {
        String email = jwtTokenProvider.getEmail(bearerAccessToken);
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("getEmail extracts the correct email from a raw JWT (no Bearer prefix)")
    void getEmail_withRawToken_returnsEmail() {
        String email = jwtTokenProvider.getEmail(rawAccessToken);
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("getEmail accepts 'bearer ' (lowercase) prefix via extractRawToken")
    void getEmail_withLowercaseBearerPrefix_returnsEmail() {
        String email = jwtTokenProvider.getEmail("bearer " + rawAccessToken);
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("getEmail throws IllegalArgumentException when token is null")
    void getEmail_withNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> jwtTokenProvider.getEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("token is required");
    }

    @Test
    @DisplayName("getEmail throws IllegalArgumentException when only 'Bearer ' with no JWT is provided")
    void getEmail_withBearerPrefixAndEmptyJwtPart_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> jwtTokenProvider.getEmail("Bearer "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid bearer token");
    }

    @Test
    @DisplayName("getEmail throws when raw token part contains an internal space after Bearer prefix")
    void getEmail_withBearerAndInternalSpaceInToken_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> jwtTokenProvider.getEmail("Bearer " + rawAccessToken + " extra"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid bearer token");
    }

    @Test
    @DisplayName("getEmail throws a JwtException when an invalid/tampered JWT is provided")
    void getEmail_withTamperedToken_throwsException() {
        String tampered = rawAccessToken.substring(0, rawAccessToken.length() - 4) + "XXXX";
        assertThatThrownBy(() -> jwtTokenProvider.getEmail(tampered))
                .isInstanceOf(Exception.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Regression / boundary: mixed-case "BeArEr" header value
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("validateToken accepts mixed-case 'BeArEr' prefix (regression for case-insensitive check)")
    void validateToken_withMixedCaseBearerPrefix_returnsTrue() {
        String mixedCaseBearer = "BeArEr " + rawAccessToken;
        assertThat(jwtTokenProvider.validateToken(mixedCaseBearer)).isTrue();
    }

    @Test
    @DisplayName("getEmail accepts mixed-case 'BeArEr' prefix (regression for case-insensitive check)")
    void getEmail_withMixedCaseBearerPrefix_returnsEmail() {
        String email = jwtTokenProvider.getEmail("BeArEr " + rawAccessToken);
        assertThat(email).isEqualTo("test@example.com");
    }
}
