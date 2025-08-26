package com.t1.achievements.dto.admin;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public record AdminStatsDto(
        long totalAchievements,
        long blockedAchievements,
        long availableAchievements,
        long activeUsers,
        long sectionsCount,
        long awardsThisMonth,

        long awardsTotal,
        double avgPerUser,
        double coveragePercent,

        List<TopAchievementDto> topPopularAllTime,
        List<TopAchievementDto> topPopular30d,
        List<RarestAchievementDto> rarestActive,
        List<MonthlyCountDto> awardsByMonth,

        List<DailyCountDto> visitsByDay30d,
        TrendDeltaDto visitsChange30d
) {}
