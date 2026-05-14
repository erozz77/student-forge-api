package com.studentforge.controller;

import com.studentforge.dto.request.UpdateProfileRequest;
import com.studentforge.dto.response.UserProfileResponse;
import com.studentforge.entity.StudentProfile;
import com.studentforge.entity.User;
import com.studentforge.enums.Role;
import com.studentforge.repository.UserRepository;
import com.studentforge.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    @Test
    void getCurrentUser_ShouldReturnUserProfile() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);

        UserProfileResponse expectedResponse = new UserProfileResponse(
                userId, "test@test.com", "Иван", "Иванов", "USER",
                null, null, null, null
        );
        when(userService.getUserProfile(userId)).thenReturn(expectedResponse);

        var result = userController.getCurrentUser(auth);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void updateProfile_ShouldUpdateUser() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);

        User user = new User();
        user.setId(userId);
        user.setEmail("test@test.com");
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setRole(Role.USER);
        user.setStudentProfile(new StudentProfile());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Петр", "Петров", null, null, null, null
        );

        UserProfileResponse expectedResponse = new UserProfileResponse(
                userId, "test@test.com", "Петр", "Петров", "USER",
                null, null, null, null
        );
        when(userService.getUserProfile(userId)).thenReturn(expectedResponse);

        var result = userController.updateProfile(auth, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateProfile_WithAdminFields_ShouldAllowUpdate() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userId);

        User user = new User();
        user.setId(userId);
        user.setEmail("admin@test.com");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setRole(Role.ADMIN);
        user.setStudentProfile(new StudentProfile());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateProfileRequest request = new UpdateProfileRequest(
                "AdminNew", "UserNew", "МГУ", "Информатика", "Группа 1", null
        );

        UserProfileResponse expectedResponse = new UserProfileResponse(
                userId, "admin@test.com", "AdminNew", "UserNew", "ADMIN",
                "МГУ", "Информатика", "Группа 1", null
        );
        when(userService.getUserProfile(userId)).thenReturn(expectedResponse);

        var result = userController.updateProfile(auth, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}