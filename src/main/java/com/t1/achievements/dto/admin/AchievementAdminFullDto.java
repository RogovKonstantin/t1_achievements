package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "AchievementAdminFullDto", description = "Максимально подробная информация об ачивке")
public record AchievementAdminFullDto(
        UUID id,
        String code,
        String title,
        String shortDescription,
        String descriptionMd,
        Integer points,
        Boolean repeatable,
        String visibility,       // enum как строка
        String iconUrl,
        String bannerUrl,
        Boolean active,
        Instant createdAt,
        Instant updatedAt,
        Integer totalSteps,      // суммарно по критериям, минимум 1
        List<AchievementCriterionDto> criteria,
        List<SectionRefDto> sections
) {}
