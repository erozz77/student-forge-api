package com.studentforge.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, max = 50, message = "Пароль должен быть от 8 до 50 символов")
        String password,

        @NotBlank(message = "Имя обязательно")
        @Size(max = 100, message = "Имя не более 100 символов")
        String firstName,

        @NotBlank(message = "Фамилия обязательна")
        @Size(max = 100, message = "Фамилия не более 100 символов")
        String lastName
) {}