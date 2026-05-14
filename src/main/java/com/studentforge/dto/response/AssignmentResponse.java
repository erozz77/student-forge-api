package com.studentforge.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        String title,
        String description,
        LocalDateTime dueDate,
        String status,
        String type,
        BigDecimal weight,
        UUID subjectId,
        String subjectName,
        Integer score
) {}