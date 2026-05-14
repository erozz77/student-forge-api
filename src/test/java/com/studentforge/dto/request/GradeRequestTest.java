package com.studentforge.dto.request;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GradeRequestTest {

    @Test
    void constructor_ShouldSetAllFields() {
        var userId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var req = new GradeRequest(userId, assignmentId, 85, "Отлично");
        assertThat(req.userId()).isEqualTo(userId);
        assertThat(req.assignmentId()).isEqualTo(assignmentId);
        assertThat(req.score()).isEqualTo(85);
        assertThat(req.comment()).isEqualTo("Отлично");
    }
}