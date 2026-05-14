package com.studentforge.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GradeResponse(
        UUID id,
        UUID userId,
        UUID assignmentId,
        String assignmentTitle,
        Integer score,
        String comment,
        Instant gradedAt
) {}