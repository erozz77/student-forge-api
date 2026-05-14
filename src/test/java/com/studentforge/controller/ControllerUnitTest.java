package com.studentforge.controller;

import com.studentforge.dto.response.AssignmentResponse;
import com.studentforge.dto.response.UserProfileResponse;
import com.studentforge.dto.request.UpdateProfileRequest;
import com.studentforge.entity.StudentProfile;
import com.studentforge.entity.User;
import com.studentforge.enums.Role;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.*;
import com.studentforge.service.AssignmentService;
import com.studentforge.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllerUnitTest {

    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private AssignmentService assignmentService;
    @Mock private AcademicPeriodRepository periodRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private SubmissionRepository submissionRepository;

    @InjectMocks private UserController userController;
    @InjectMocks private AssignmentController assignmentController;
    @InjectMocks private AdminController adminController;

    @Test
    void getCurrentUser_ShouldReturnProfile() {
        var userId = UUID.randomUUID();
        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);
        when(userService.getUserProfile(userId)).thenReturn(
                new UserProfileResponse(userId, "t@t.com", "A", "B", "USER", null, null, null, null));
        var result = userController.getCurrentUser(auth);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAssignments_ShouldReturnPage() {
        var subjectId = UUID.randomUUID();
        when(assignmentService.getAssignmentsBySubject(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        var result = assignmentController.getAssignments(subjectId, null, Pageable.unpaged());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAssignments_WithStatus_ShouldReturnPage() {
        var subjectId = UUID.randomUUID();
        when(assignmentService.getAssignmentsBySubjectAndStatus(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        var result = assignmentController.getAssignments(subjectId, "GRADED", Pageable.unpaged());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateProfile_ShouldReturnProfile() {
        var userId = UUID.randomUUID();
        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);
        var user = User.builder().email("t@t.com").firstName("A").lastName("B").role(Role.USER).build();
        user.setId(userId);
        user.setStudentProfile(new StudentProfile());
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userService.getUserProfile(userId)).thenReturn(
                new UserProfileResponse(userId, "t@t.com", "A", "B", "USER", null, null, null, null));
        var req = new UpdateProfileRequest("Иван", null, null, null, null, null);
        var result = userController.updateProfile(auth, req);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getStudents_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of());
        var result = adminController.students();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPeriods_ShouldReturnList() {
        when(periodRepository.findAll()).thenReturn(List.of());
        var result = adminController.periods();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getGroups_ShouldReturnList() {
        when(groupRepository.findAll()).thenReturn(List.of());
        var result = adminController.groups();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getSubjects_ShouldReturnList() {
        when(subjectRepository.findAll()).thenReturn(List.of());
        var result = adminController.subjects();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getSubmissions_Admin_ShouldReturnList() {
        when(submissionRepository.findAll()).thenReturn(List.of());
        var result = adminController.submissions();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAssignments_Admin_ShouldReturnList() {
        when(assignmentRepository.findAll()).thenReturn(List.of());
        var result = adminController.assignments();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}