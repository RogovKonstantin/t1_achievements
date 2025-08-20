package com.t1.achievements.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "ProfileViewDto", description = "Экран профиля с ачивками пользователя")
public record ProfileViewDto(
        ApiUserDto user,
        int unlockedCount,
        List<SectionWithCardsDto> sections
) {}
