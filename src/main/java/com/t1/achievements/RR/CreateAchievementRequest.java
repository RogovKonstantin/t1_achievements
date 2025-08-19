package com.t1.achievements.RR;

import java.util.List;
import java.util.UUID;

public record CreateAchievementRequest(
        String title,
        String shortDescription,
        String descriptionMd,
        UUID sectionId,
        UUID iconAssetId,
        String visibility,   // PUBLIC/PRIVATE/HIDDEN
        Boolean active,
        List<CriterionInput> criteria
) {}
