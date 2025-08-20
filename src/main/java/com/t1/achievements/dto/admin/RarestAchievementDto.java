package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "RarestAchievementDto", description = "Самые редкие (по доле обладателей) активные ачивки")
public record RarestAchievementDto(
        UUID id,
        String title,
        double rarityPercent
) {}
