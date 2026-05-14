package com.studentforge.service;

import com.studentforge.dto.response.UserProfileResponse;
import com.studentforge.entity.StudentProfile;
import com.studentforge.entity.User;
import com.studentforge.enums.Role;
import com.studentforge.exception.ResourceNotFoundException;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .email("test@student.com")
                .passwordHash("hashed_pass")
                .firstName("Иван")
                .lastName("Иванов")
                .role(Role.USER)
                .build();
        mockUser.setId(userId);
        mockUser.setStudentProfile(new StudentProfile());
    }

    @Test
    void getUserProfile_ShouldReturnProfile_WhenUserExists() {
        var expectedResponse = new UserProfileResponse(userId, "test@student.com", "Иван", "Иванов", "USER", null, null, null, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserProfileResponse(mockUser)).thenReturn(expectedResponse);
        var result = userService.getUserProfile(userId);
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@student.com");
        assertThat(result.firstName()).isEqualTo("Иван");
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfile(userId));
    }

    @Test
    void createUser_ShouldSaveUserWithHashedPassword() {
        when(userRepository.existsByEmail("test@student.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        var result = userService.createUser("test@student.com", "password123", "Иван", "Иванов");
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@student.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashed_pass");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getStudentProfile()).isNotNull();
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByEmail("test@student.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("test@student.com", "password123", "Иван", "Иванов"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        when(userRepository.findByEmail("test@student.com")).thenReturn(Optional.of(mockUser));
        var result = userService.findByEmail("test@student.com");
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@student.com");
    }

    @Test
    void findByEmail_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findByEmail("notfound@test.com"));
    }

    @Test
    void findUserById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        var result = userService.findUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void findUserById_ShouldThrowException_WhenNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(userId));
    }
}