package com.t1.achievements.RR;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateSectionRequest(
        @Schema(description = "Название раздела", example = "Сообщество")
        @NotBlank String name,

        @Schema(description = "Описание раздела", example = "Ивенты, статьи, волонтёрство")
        @NotBlank String description
) {}
