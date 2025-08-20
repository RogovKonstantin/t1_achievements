package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "TopAchievementDto", description = "Топ ачивок по количеству награждений")
public record TopAchievementDto(
        UUID id,
        String title,
        long awardsCount
) {}
