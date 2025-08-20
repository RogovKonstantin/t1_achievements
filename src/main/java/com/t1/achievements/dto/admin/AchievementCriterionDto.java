package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "AchievementCriterionDto", description = "Критерий получения ачивки")
public record AchievementCriterionDto(
        UUID id,
        ActivityTypeRefDto activityType,
        Integer requiredCount,
        Integer withinDays,
        String descriptionOverride,
        Integer sortOrder
) {}
