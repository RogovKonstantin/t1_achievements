package com.t1.achievements.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Лента активности (награды пользователя)")
public record ActivityFeedDto(
        @Schema(description = "Список событий, отсортированных по времени выдана DESC")
        List<ActivityEventDto> items
) {}
