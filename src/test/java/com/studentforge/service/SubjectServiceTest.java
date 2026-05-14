package com.studentforge.service;

import com.studentforge.dto.response.SubjectResponse;
import com.studentforge.entity.StudentProfile;
import com.studentforge.entity.Subject;
import com.studentforge.entity.User;
import com.studentforge.enums.Role;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.SubjectRepository;
import com.studentforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private SubjectRepository subjectRepository;

    @InjectMocks private UserService userService;
    @InjectMocks private SubjectService subjectService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .email("test@test.com")
                .passwordHash("hash")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();
        mockUser.setId(userId);
        mockUser.setStudentProfile(new StudentProfile());
    }

    @Test
    void getUserProfileShouldMapUniversityAndMajor() {
        mockUser.getStudentProfile().setUniversity("МГУ");
        mockUser.getStudentProfile().setMajor("ПИ");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserProfileResponse(mockUser)).thenReturn(
                new com.studentforge.dto.response.UserProfileResponse(userId, "t@t.com", "T", "U", "USER", "МГУ", "ПИ", "ЦИС-23", null));

        var result = userService.getUserProfile(userId);
        assertThat(result.university()).isEqualTo("МГУ");
        assertThat(result.major()).isEqualTo("ПИ");
    }

    @Test
    void findUserByIdShouldUseRepository() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        var result = userService.findUserById(userId);
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        verify(userRepository).findById(userId);
    }

    @Test
    void findByEmailShouldUseRepository() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        var result = userService.findByEmail("test@test.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        verify(userRepository).findByEmail("test@test.com");
    }

    @Test
    void getSubjectsByGroup_ShouldReturnPage() {
        UUID groupId = UUID.randomUUID();
        var subject = Subject.builder()
                .name("Java")
                .teacherName("Иванов")
                .weightCoefficient(BigDecimal.ONE)
                .totalHours(100)
                .build();
        subject.setId(UUID.randomUUID());
        Page<Subject> page = new PageImpl<>(List.of(subject));

        var response = new SubjectResponse(subject.getId(), "Java", "Иванов", BigDecimal.ONE, 100, groupId);

        when(subjectRepository.findAllByGroupId(any(), any())).thenReturn(page);
        when(userMapper.toSubjectResponse(subject)).thenReturn(response);

        var result = subjectService.getSubjectsByGroup(groupId, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}