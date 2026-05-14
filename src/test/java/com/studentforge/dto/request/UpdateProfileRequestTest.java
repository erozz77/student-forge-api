package com.studentforge.dto.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProfileRequestTest {

    @Test
    void constructor_ShouldSetAllFields() {
        var req = new UpdateProfileRequest("Иван", "Иванов", "ЯГТУ", "ИС", "ЦИС-23", null);
        assertThat(req.firstName()).isEqualTo("Иван");
        assertThat(req.lastName()).isEqualTo("Иванов");
        assertThat(req.university()).isEqualTo("ЯГТУ");
        assertThat(req.major()).isEqualTo("ИС");
        assertThat(req.groupName()).isEqualTo("ЦИС-23");
    }

    @Test
    void constructor_ShouldAllowNulls() {
        var req = new UpdateProfileRequest(null, null, null, null, null, null);
        assertThat(req.firstName()).isNull();
    }
}