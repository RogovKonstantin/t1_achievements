package com.t1.achievements.RR;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record UpdateAchievementRequest(
        Optional<String> title,
        Optional<String> descriptionMd,
        Optional<@Valid List<@NotNull UUID>> sectionIds,
        Optional<@Valid List<@Valid CriterionInput>> criteria,
        Optional<Integer> points
) {}
