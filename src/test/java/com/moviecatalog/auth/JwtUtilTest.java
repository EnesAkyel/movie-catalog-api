package com.moviecatalog.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {
    private static final String SECRET = "c2VjcmV0LWtleS1mb3ItbW92aWUtY2F0YWxvZy1hcGk=";
    private static final String OTHER_SECRET = "ZGlmZmVyZW50LWtleS1mb3ItbW92aWUtY2F0YWxvZy1hcGk=";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 3_600_000L);
    }

    @Test
    @DisplayName("generateToken returns a non-null signed JWT string")
    void generateToken_returnsSignedToken() {
        assertThat(jwtUtil.generateToken("admin")).isNotNull().contains(".");
    }

    @Test
    @DisplayName("extractUsername returns the subject embedded in the token")
    void extractUsername_returnsCorrectSubject() {
        String token = jwtUtil.generateToken("admin");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("isValid returns true for a freshly generated token")
    void isValid_withValidToken_returnsTrue() {
        assertThat(jwtUtil.isValid(jwtUtil.generateToken("admin"))).isTrue();
    }

    @Test
    @DisplayName("isValid returns false for a malformed token string")
    void isValid_withMalformedToken_returnsFalse() {
        assertThat(jwtUtil.isValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("isValid returns false for a token signed with a different key")
    void isValid_withWrongSignature_returnsFalse() {
        String tokenFromOtherKey = new JwtUtil(OTHER_SECRET, 3_600_000L).generateToken("admin");
        assertThat(jwtUtil.isValid(tokenFromOtherKey)).isFalse();
    }

    @Test
    @DisplayName("isValid returns false for an already-expired token")
    void isValid_withExpiredToken_returnsFalse() {
        String expiredToken = new JwtUtil(SECRET, -1_000L).generateToken("admin");
        assertThat(jwtUtil.isValid(expiredToken)).isFalse();
    }
}
