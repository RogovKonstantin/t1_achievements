package com.t1.achievements.dto;


import java.util.UUID;

public record UserListItemDto(
        UUID id,
        String fullName,
        String department,
        String position,
        String avatarUrl
) {}
