package com.studentforge.security;

import com.studentforge.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        var config = new JwtConfig();
        config.setSecretKey("dGhpc0lzQVZlcnlMb25nQW5kU2VjdXJlU2VjcmV0S2V5Rm9yU3R1ZGVudEZvcmdlQXBw");
        config.setAccessTokenExpiration(3600000);
        config.setRefreshTokenExpiration(604800000);
        tokenProvider = new JwtTokenProvider(config);
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateAccessToken(userId, "test@test.com", "USER");
        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateRefreshToken(userId);
        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getUserIdFromToken_ShouldReturnCorrectId() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateAccessToken(userId, "test@test.com", "USER");
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateAccessToken(userId, "test@test.com", "USER");
        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("test@test.com");
    }

    @Test
    void getRoleFromToken_ShouldReturnCorrectRole() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateAccessToken(userId, "test@test.com", "ADMIN");
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnFalse() {
        assertThat(tokenProvider.validateToken("invalid.token.here")).isFalse();
    }
}