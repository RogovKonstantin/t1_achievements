package com.t1.achievements.RR;

public record CriterionInput(
        String typeCode,
        Integer value,
        Integer withinDays,
        String descriptionOverride,
        Integer sortOrder
) {}
