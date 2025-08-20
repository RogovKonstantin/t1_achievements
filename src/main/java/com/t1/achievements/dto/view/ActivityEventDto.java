package com.t1.achievements.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Событие в ленте активности пользователя (награда)")
public record ActivityEventDto(
        @Schema(description = "ID ачивки") UUID achievementId,
        @Schema(description = "Название ачивки") String title,
        @Schema(description = "URL иконки ачивки") String iconUrl,
        @Schema(description = "Когда выдана", format = "date-time") Instant awardedAt,
        @Schema(description = "Человеко-понятное сообщение", example = "Получена ачивка «5 лет с нами».")
        String message
) {}
