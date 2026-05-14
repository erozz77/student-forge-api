package com.studentforge.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GradeRequest(
        @NotNull UUID userId,
        @NotNull UUID assignmentId,
        @NotNull @Min(0) @Max(100) Integer score,
        String comment
) {}