package com.studentforge.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OpenApiConfigTest {

    @Autowired
    private OpenApiConfig openApiConfig;

    @Test
    void customOpenAPI_ShouldNotBeNull() {
        var api = openApiConfig.customOpenAPI();
        assertThat(api).isNotNull();
        assertThat(api.getInfo().getTitle()).isEqualTo("StudyForge");
        assertThat(api.getInfo().getVersion()).isEqualTo("1.0");
    }
}