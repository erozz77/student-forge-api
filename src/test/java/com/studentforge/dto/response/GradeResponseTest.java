package com.studentforge.dto.response;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GradeResponseTest {

    @Test
    void constructor_ShouldSetAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var now = Instant.now();
        var resp = new GradeResponse(id, userId, assignmentId, "Лаб 1", 90, "Хорошо", now);
        assertThat(resp.id()).isEqualTo(id);
        assertThat(resp.score()).isEqualTo(90);
        assertThat(resp.assignmentTitle()).isEqualTo("Лаб 1");
        assertThat(resp.comment()).isEqualTo("Хорошо");
    }
}