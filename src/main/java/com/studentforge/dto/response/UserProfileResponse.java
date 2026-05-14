package com.studentforge.dto.response;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        String university,
        String major,
        String groupName,
        String avatarUrl
) {}