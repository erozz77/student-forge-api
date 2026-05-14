package com.studentforge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtTokenProvider tokenProvider;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        var config = new com.studentforge.config.JwtConfig();
        config.setSecretKey("dGhpc0lzQVZlcnlMb25nQW5kU2VjdXJlU2VjcmV0S2V5Rm9yU3R1ZGVudEZvcmdlQXBw");
        config.setAccessTokenExpiration(3600000);
        config.setRefreshTokenExpiration(604800000);
        tokenProvider = new JwtTokenProvider(config);
        filter = new JwtAuthFilter(tokenProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_NoToken_ShouldProceedWithoutAuth() throws ServletException, IOException {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_ValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        String token = tokenProvider.generateAccessToken(java.util.UUID.randomUUID(), "t@t.com", "USER");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void doFilter_InvalidToken_ShouldProceedWithoutAuth() throws ServletException, IOException {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}