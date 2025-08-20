package com.t1.achievements.dto.admin;

import java.util.UUID;

public record UserAchievementGrantDto(
        UUID userId,
        UUID achievementId,
        String awardedAt
) {}
