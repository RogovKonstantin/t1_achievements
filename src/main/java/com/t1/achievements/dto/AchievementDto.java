package com.t1.achievements.dto;

import java.util.List;
import java.util.UUID;

public record AchievementDto(
        UUID id,
        String title,
        String shortDescription,
        String descriptionMd,
        List<UUID> sectionIds,
        UUID iconAssetId,
        String visibility,
        Boolean active
) {}

