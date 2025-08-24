// com.t1.achievements.RR.CreateAchievementRequest
package com.t1.achievements.RR;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record CreateAchievementRequest(
        @NotBlank String title,
        @NotBlank String descriptionMd,
        @NotNull  @Size(min = 1) List<@NotNull UUID> sectionIds,           // ⬅️ несколько категорий
        @NotNull  @Size(min = 1) List<@Valid CriterionInput> criteria,
        @NotNull  @PositiveOrZero Integer points
) {}
