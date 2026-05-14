package com.studentforge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationStartupTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethod_ShouldNotThrowException() {
        StudentForgeApiApplication.main(new String[]{});
        assertThat(true).isTrue();
    }
}