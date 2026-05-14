package com.studentforge.controller;

import com.studentforge.dto.request.UpdateProfileRequest;
import com.studentforge.dto.response.UserProfileResponse;
import com.studentforge.entity.StudentProfile;
import com.studentforge.entity.User;
import com.studentforge.repository.UserRepository;
import com.studentforge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile((UUID) authentication.getPrincipal()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow();
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());

        StudentProfile profile = user.getStudentProfile();
        if (profile == null) { profile = new StudentProfile(); profile.setUser(user); }

        if (isAdmin) {
            if (request.university() != null) profile.setUniversity(request.university());
            if (request.major() != null) profile.setMajor(request.major());
            if (request.groupName() != null) profile.setGroupName(request.groupName());
        }
        if (request.avatarUrl() != null) profile.setAvatarUrl(request.avatarUrl());

        user.setStudentProfile(profile);
        userRepository.save(user);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PostMapping("/me/avatar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> uploadAvatar(Authentication authentication, @RequestParam("file") MultipartFile file) {
        UUID userId = (UUID) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow();
        try {
            String fn = "avatar_" + userId + "_" + System.currentTimeMillis() + ".png";
            Path up = Path.of("uploads/avatars"); Files.createDirectories(up);
            Files.copy(file.getInputStream(), up.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
            StudentProfile p = user.getStudentProfile();
            if (p == null) { p = new StudentProfile(); p.setUser(user); }
            p.setAvatarUrl("/uploads/avatars/" + fn);
            user.setStudentProfile(p);
            userRepository.save(user);
            return ResponseEntity.ok("/uploads/avatars/" + fn);
        } catch (IOException e) { return ResponseEntity.status(500).body("Ошибка"); }
    }
}