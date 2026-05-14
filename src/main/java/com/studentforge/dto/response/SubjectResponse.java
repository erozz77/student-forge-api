package com.studentforge.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record SubjectResponse(
        UUID id,
        String name,
        String teacherName,
        BigDecimal weightCoefficient,
        Integer totalHours,
        UUID groupId
) {}