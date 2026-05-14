package com.studentforge.dto.response;

import java.util.List;
import java.util.UUID;

public record SubjectStatsResponse(
        UUID subjectId,
        String subjectName,
        String teacherName,
        Double averageScore,
        Integer totalGrades,
        List<AssignmentGradeDto> assignments
) {
    public record AssignmentGradeDto(
            UUID assignmentId,
            String assignmentTitle,
            Integer score,
            String gradedAt
    ) {}
}