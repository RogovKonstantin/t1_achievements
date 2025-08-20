package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MonthlyCountDto", description = "Количество награждений по месяцам")
public record MonthlyCountDto(
        @Schema(example = "2025-07") String month,
        @Schema(example = "12") long count
) {}
