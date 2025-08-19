package com.t1.achievements.dto;

import java.util.UUID;

public record AchievementDto(
        UUID id,
        String title,
        String shortDescription,
        String descriptionMd,
        UUID sectionId,
        UUID iconAssetId,
        String visibility,
        Boolean active
) {}

