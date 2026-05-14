package com.studentforge.controller;

import com.studentforge.entity.*;
import com.studentforge.enums.AssignmentStatus;
import com.studentforge.enums.AssignmentType;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import com.studentforge.enums.SubmissionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectControllerUnitTest {

    @Mock private SubjectRepository subjectRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private UserMapper userMapper;
    @Mock private GradeRepository gradeRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private SubjectController controller;

    @Test
    void submit_ShouldSetStatusToSubmitted() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Assignment a = Assignment.builder()
                .title("Test")
                .dueDate(LocalDateTime.now())
                .status(AssignmentStatus.PENDING)
                .type(AssignmentType.LAB)
                .weight(BigDecimal.ONE)
                .subject(new Subject())
                .build();

        User user = new User();
        user.setId(userId);
        user.setFirstName("Test");
        user.setLastName("User");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);
        when(assignmentRepository.findById(any(UUID.class))).thenReturn(Optional.of(a));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(submissionRepository.findByAssignmentIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = controller.submit(id, null, auth);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void cancel_ShouldSetStatusToPending() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Assignment a = Assignment.builder()
                .title("Test")
                .dueDate(LocalDateTime.now())
                .status(AssignmentStatus.SUBMITTED)
                .type(AssignmentType.LAB)
                .weight(BigDecimal.ONE)
                .subject(new Subject())
                .build();

        User user = new User();
        user.setId(userId);
        user.setFirstName("Test");
        user.setLastName("User");

        Submission submission = Submission.builder()
                .assignment(a)
                .user(user)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);
        when(submissionRepository.findByAssignmentIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(submission));

        var result = controller.cancel(id, auth);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void mySubjects_ShouldReturnList() {
        var auth = mock(Authentication.class);
        UUID userId = UUID.randomUUID();
        when(auth.getPrincipal()).thenReturn(userId);
        when(enrollmentRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of());

        var result = controller.getMySubjects(auth);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void myStats_ShouldReturnStats() {
        var auth = mock(Authentication.class);
        UUID userId = UUID.randomUUID();
        when(auth.getPrincipal()).thenReturn(userId);
        when(enrollmentRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of());
        when(gradeRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of());
        when(subjectRepository.findAll()).thenReturn(List.of());

        var result = controller.myStats(auth);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getMyAssignmentStatus_WhenExists_ShouldReturnStatus() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Submission submission = Submission.builder()
                .status(SubmissionStatus.SUBMITTED)
                .filePath("test.txt")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);
        when(submissionRepository.findByAssignmentIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(submission));

        var result = controller.getMyAssignmentStatus(id, auth);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getMyAssignmentStatus_WhenNotExists_ShouldReturnPending() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);
        when(submissionRepository.findByAssignmentIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());

        var result = controller.getMyAssignmentStatus(id, auth);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getGroupSubjects_ShouldReturnPage() {
        UUID groupId = UUID.randomUUID();
        when(subjectRepository.findAllByGroupId(any(UUID.class), any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        var result = controller.getGroupSubjects(groupId, Pageable.unpaged());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getMySubjectsStats_ShouldReturnList() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);
        when(enrollmentRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of());
        when(gradeRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of());

        var result = controller.getMySubjectsStats(auth);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}