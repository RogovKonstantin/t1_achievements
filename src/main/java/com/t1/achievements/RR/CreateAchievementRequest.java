package com.t1.achievements.RR;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


public record CreateAchievementRequest(
        @NotBlank String title,
        @NotBlank String descriptionMd,
        @NotNull  UUID sectionId,
        @NotNull  @Size(min = 1) List<@Valid CriterionInput> criteria,
        @NotNull  @PositiveOrZero Integer points
) {}



