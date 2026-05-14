package com.studentforge.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoder_ShouldEncodeAndMatch() {
        String raw = "password123";
        String encoded = passwordEncoder.encode(raw);
        assertThat(encoded).isNotEqualTo(raw);
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
    }

    @Test
    void passwordEncoder_ShouldNotMatchWrongPassword() {
        String encoded = passwordEncoder.encode("password123");
        assertThat(passwordEncoder.matches("wrong", encoded)).isFalse();
    }
}