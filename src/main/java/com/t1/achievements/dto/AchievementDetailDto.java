package com.t1.achievements.dto;

import java.time.Instant;
import java.util.UUID;

public record AchievementDetailDto(
        UUID id,
        String code,
        String title,
        String shortDescription,
        String descriptionMd,
        String animationUrl,
        boolean awarded,
        Instant awardedAt,
        String awardMethod,
        int currentStep,
        int totalSteps,
        double rarityPercent
) {}
