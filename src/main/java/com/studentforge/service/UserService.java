package com.studentforge.service;

import com.studentforge.dto.response.UserProfileResponse;
import com.studentforge.entity.StudentProfile;
import com.studentforge.entity.User;
import com.studentforge.enums.Role;
import com.studentforge.exception.ResourceNotFoundException;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getUserProfile(UUID userId) {
        User user = findUserById(userId);
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional
    public User createUser(String email, String rawPassword, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.USER)
                .build();

        StudentProfile profile = StudentProfile.builder()
                .user(user)
                .build();

        user.setStudentProfile(profile);
        log.info("Created new user with email: {}", email);
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}