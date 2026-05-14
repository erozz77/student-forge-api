package com.studentforge.dto.request;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String university,
        String major,
        String groupName,
        String avatarUrl
) {}