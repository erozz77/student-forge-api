package com.studentforge.controller;

import com.studentforge.dto.response.AssignmentResponse;
import com.studentforge.service.AssignmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerUnitTest {

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private AssignmentController assignmentController;

    @Test
    void getAssignments_ShouldReturnPage() {
        UUID subjectId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentResponse> expectedPage = new PageImpl<>(List.of());

        when(assignmentService.getAssignmentsBySubject(eq(subjectId), any(Pageable.class)))
                .thenReturn(expectedPage);

        var result = assignmentController.getAssignments(subjectId, null, pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAssignments_WithStatus_ShouldReturnPage() {
        UUID subjectId = UUID.randomUUID();
        String status = "GRADED";
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentResponse> expectedPage = new PageImpl<>(List.of());

        when(assignmentService.getAssignmentsBySubjectAndStatus(eq(subjectId), eq(status), any(Pageable.class)))
                .thenReturn(expectedPage);

        var result = assignmentController.getAssignments(subjectId, status, pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}