package com.studentforge.service;

import com.studentforge.dto.response.AssignmentResponse;
import com.studentforge.entity.Assignment;
import com.studentforge.entity.Subject;
import com.studentforge.enums.AssignmentStatus;
import com.studentforge.enums.AssignmentType;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.AssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private UserMapper userMapper;
    @InjectMocks private AssignmentService assignmentService;

    @Test
    void getAssignmentsBySubjectShouldReturnPage() {
        UUID subjectId = UUID.randomUUID();
        Assignment a = Assignment.builder()
                .title("Test")
                .dueDate(LocalDateTime.now())
                .status(AssignmentStatus.PENDING)
                .type(AssignmentType.LAB)
                .weight(BigDecimal.ONE)
                .subject(new Subject())
                .build();
        Page<Assignment> page = new PageImpl<>(List.of(a));
        var expected = new AssignmentResponse(UUID.randomUUID(), "Test", "", LocalDateTime.now(), "PENDING", "LAB", BigDecimal.ONE, subjectId, "Java", null);

        when(assignmentRepository.findAllBySubjectId(subjectId, Pageable.unpaged())).thenReturn(page);
        when(userMapper.toAssignmentResponse(a)).thenReturn(expected);

        var result = assignmentService.getAssignmentsBySubject(subjectId, Pageable.unpaged());
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(assignmentRepository).findAllBySubjectId(subjectId, Pageable.unpaged());
    }

    @Test
    void getAssignmentsBySubjectAndStatusShouldFilter() {
        UUID subjectId = UUID.randomUUID();
        Assignment a = Assignment.builder()
                .title("Filtered")
                .dueDate(LocalDateTime.now())
                .status(AssignmentStatus.GRADED)
                .type(AssignmentType.EXAM)
                .weight(BigDecimal.ONE)
                .subject(new Subject())
                .build();
        Page<Assignment> page = new PageImpl<>(List.of(a));

        when(assignmentRepository.findAllBySubjectIdAndStatus(subjectId, "GRADED", Pageable.unpaged())).thenReturn(page);
        when(userMapper.toAssignmentResponse(a)).thenReturn(
                new AssignmentResponse(UUID.randomUUID(), "Filtered", "", LocalDateTime.now(), "GRADED", "EXAM", BigDecimal.ONE, subjectId, "Java", 85));

        var result = assignmentService.getAssignmentsBySubjectAndStatus(subjectId, "GRADED", Pageable.unpaged());
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(assignmentRepository).findAllBySubjectIdAndStatus(subjectId, "GRADED", Pageable.unpaged());
    }
}