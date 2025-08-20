package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "AdminStatsDto", description = "Сводная админская статистика")
public record AdminStatsDto(
        // Карточки дашборда
        @Schema(example = "3") long totalAchievements,
        @Schema(example = "0") long blockedAchievements,
        @Schema(example = "3") long availableAchievements,
        @Schema(example = "5") long activeUsers,
        @Schema(example = "3") long sectionsCount,
        @Schema(example = "5") long awardsThisMonth,

        // Обобщённые метрики
        @Schema(example = "27") long awardsTotal,
        @Schema(example = "5.4") double avgAchievementsPerActiveUser,
        @Schema(example = "80.0", description = "Доля активных пользователей, у которых есть хотя бы одна ачивка, %")
        double userCoveragePercent,

        // Топы
        List<TopAchievementDto> topPopularAllTime,
        List<TopAchievementDto> topPopular30d,

        // «Редкие» ачивки (минимальная редкость среди активных)
        List<RarestAchievementDto> rarestActive,

        // Серия по месяцам (последние N месяцев)
        List<MonthlyCountDto> awardsByMonth
) {}
