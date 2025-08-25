package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "AdminStatsDto", description = "Сводная админская статистика")
public record AdminStatsDto(
        @Schema(example = "3") long totalAchievements,
        @Schema(example = "0") long blockedAchievements,
        @Schema(example = "3") long availableAchievements,
        @Schema(example = "5") long activeUsers,
        @Schema(example = "3") long sectionsCount,
        @Schema(example = "5") long awardsThisMonth,

        @Schema(example = "27") long awardsTotal,
        @Schema(example = "5.4") double avgAchievementsPerActiveUser,
        @Schema(example = "80.0", description = "Доля активных пользователей, у которых есть хотя бы одна ачивка, %")
        double userCoveragePercent,

        List<TopAchievementDto> topPopularAllTime,
        List<TopAchievementDto> topPopular30d,

        List<RarestAchievementDto> rarestActive,

        List<MonthlyCountDto> awardsByMonth
) {}
