package com.studentforge.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void secretKey_ShouldNotBeNull() {
        assertThat(jwtConfig.getSecretKey()).isNotBlank();
    }

    @Test
    void accessTokenExpiration_ShouldBePositive() {
        assertThat(jwtConfig.getAccessTokenExpiration()).isPositive();
    }
}