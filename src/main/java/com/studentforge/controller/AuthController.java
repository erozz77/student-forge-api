package com.studentforge.controller;

import com.studentforge.dto.request.LoginRequest;
import com.studentforge.dto.request.RegisterRequest;
import com.studentforge.dto.response.AuthResponse;
import com.studentforge.entity.User;
import com.studentforge.security.JwtTokenProvider;
import com.studentforge.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.studentforge.exception.ResourceNotFoundException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.email());

        User user = userService.createUser(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.findByEmail(request.email());
            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Неверный пароль"));
            }
            String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
            String refreshToken = tokenProvider.generateRefreshToken(user.getId());
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getRole().name()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Пользователь с таким email не найден"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = bearerToken.substring(7);

        if (!tokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findUserById(tokenProvider.getUserIdFromToken(token));
        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, user.getId(), user.getEmail(), user.getRole().name()));
    }
}