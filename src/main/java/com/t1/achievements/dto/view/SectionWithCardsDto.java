package com.t1.achievements.dto.view;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(name = "SectionWithCardsDto", description = "Секция на карте ачивок с карточками")
public record SectionWithCardsDto(
        UUID id,
        String name,
        String description,
        List<AchievementCardDto> achievements
) {}

