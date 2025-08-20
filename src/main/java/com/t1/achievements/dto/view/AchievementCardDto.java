package com.t1.achievements.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "AchievementCardDto", description = "Карточка ачивки в секции")
public record AchievementCardDto(
        UUID id,
        String title,
        String iconUrl,
        int currentStep,
        int totalSteps,
        boolean awarded,
        double rarityPercent
) {}
