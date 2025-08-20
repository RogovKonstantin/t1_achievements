package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "ActivityTypeRefDto")
public record ActivityTypeRefDto(
        UUID id,
        String code,
        String name
) {}
