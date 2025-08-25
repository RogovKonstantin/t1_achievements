package com.t1.achievements.RR;

import java.util.UUID;

public record AchievementCreatedEvent(
        UUID achievementId,
        boolean massSeed
) {}
