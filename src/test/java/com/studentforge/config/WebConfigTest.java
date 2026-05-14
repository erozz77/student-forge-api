package com.studentforge.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WebConfigTest {

    @Autowired
    private WebConfig webConfig;

    @Test
    void webConfig_ShouldExist() {
        assertThat(webConfig).isNotNull();
    }
}