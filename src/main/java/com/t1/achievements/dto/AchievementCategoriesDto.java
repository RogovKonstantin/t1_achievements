package com.t1.achievements.dto;


import java.util.List;
import java.util.UUID;

public record AchievementCategoriesDto(
        UUID achievementId,
        List<SectionShortDto> categories
) {}

