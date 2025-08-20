package com.t1.achievements.service;

import com.t1.achievements.dto.admin.*;
import com.t1.achievements.entity.Achievement;
import com.t1.achievements.repository.AchievementRepository;
import com.t1.achievements.repository.SectionRepository;
import com.t1.achievements.repository.UserAchievementRepository;
import com.t1.achievements.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final AchievementRepository achievementRepo;
    private final SectionRepository sectionRepo;
    private final UserRepository userRepo;
    private final UserAchievementRepository userAchRepo;

    @Transactional(readOnly = true)
    public AdminStatsDto getDashboardStats() {
        long totalAchievements = achievementRepo.count();
        // активные ачивки есть через твой метод findAllActiveWithDeps()
        int activeAchievements = achievementRepo.findAllActiveWithDeps().size();
        long blockedAchievements = totalAchievements - activeAchievements;
        long availableAchievements = activeAchievements;

        long sectionsCount = sectionRepo.findByActiveTrueOrderBySortOrderAsc().size();
        long activeUsers = Math.max(0, userRepo.countActive());

        // суммарное число награждений (всё время) — из уже имеющейся агрегации
        Map<UUID, Long> awardedCounts = userAchRepo.countAwardedByAchievement()
                .stream()
                .collect(Collectors.toMap(
                        UserAchievementRepository.AwardStat::getAchievementId,
                        UserAchievementRepository.AwardStat::getAwardedCount
                ));
        long awardsTotal = awardedCounts.values().stream().mapToLong(Long::longValue).sum();

        // Среднее число ачивок на активного пользователя
        double avgPerUser = activeUsers == 0 ? 0.0 : (double) awardsTotal / (double) activeUsers;

        // Покрытие пользователей (у кого есть хотя бы одна ачивка)
        long usersWithAnyAwards = userAchRepo.countUsersWithAnyAwards();
        double coverage = (activeUsers == 0) ? 0.0 : 100.0 * (double) usersWithAnyAwards / (double) activeUsers;

        // За месяц
        Instant startOfMonth = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1)
                .atStartOfDay().toInstant(ZoneOffset.UTC);
        long awardsThisMonth = userAchRepo.countAwardedSince(startOfMonth);

        // ТОП-5 по популярности (всё время)
        var topAllTime = userAchRepo.findTopAwardedAllTime(PageRequest.of(0, 5));
        Map<UUID, Achievement> byIdAll = achievementRepo.findAllById(
                topAllTime.stream().map(UserAchievementRepository.TopAward::getAchievementId).toList()
        ).stream().collect(Collectors.toMap(Achievement::getId, Function.identity()));
        List<TopAchievementDto> topPopularAllTime = topAllTime.stream()
                .map(t -> new TopAchievementDto(t.getAchievementId(),
                        Optional.ofNullable(byIdAll.get(t.getAchievementId())).map(Achievement::getTitle).orElse(""),
                        t.getCnt()))
                .toList();

        // ТОП-5 за 30 дней
        Instant from30 = Instant.now().minus(Duration.ofDays(30));
        var top30 = userAchRepo.findTopAwardedSince(from30, PageRequest.of(0, 5));
        Map<UUID, Achievement> byId30 = achievementRepo.findAllById(
                top30.stream().map(UserAchievementRepository.TopAward::getAchievementId).toList()
        ).stream().collect(Collectors.toMap(Achievement::getId, Function.identity()));
        List<TopAchievementDto> topPopular30d = top30.stream()
                .map(t -> new TopAchievementDto(t.getAchievementId(),
                        Optional.ofNullable(byId30.get(t.getAchievementId())).map(Achievement::getTitle).orElse(""),
                        t.getCnt()))
                .toList();

        // Самые "редкие" активные ачивки (минимальная доля обладателей)
        // редкость = 100 * (awardedCount / totalUsers)
        long totalUsers = Math.max(1, userRepo.countActive());
        Set<UUID> activeAchIds = achievementRepo.findAllActiveWithDeps().stream()
                .map(Achievement::getId).collect(Collectors.toSet());
        List<RarestAchievementDto> rarestActive = awardedCounts.entrySet().stream()
                .filter(e -> activeAchIds.contains(e.getKey()))
                .map(e -> {
                    UUID id = e.getKey();
                    Achievement a = byIdAll.get(id);
                    if (a == null) a = achievementRepo.findById(id).orElse(null);
                    String title = a != null ? a.getTitle() : "";
                    double rarity = 100.0 * (e.getValue() / (double) totalUsers);
                    return new RarestAchievementDto(id, title, rarity);
                })
                .sorted(Comparator.comparingDouble(RarestAchievementDto::rarityPercent))
                .limit(5)
                .toList();

        // По месяцам — последние 6 месяцев
        Instant from6m = YearMonth.now(ZoneOffset.UTC).minusMonths(5)
                .atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        var monthBuckets = userAchRepo.countAwardsByMonthSince(from6m);
        List<MonthlyCountDto> awardsByMonth = monthBuckets.stream()
                .map(mb -> new MonthlyCountDto(mb.getMonth(), mb.getCnt()))
                .toList();

        return new AdminStatsDto(
                totalAchievements,
                blockedAchievements,
                availableAchievements,
                activeUsers,
                sectionsCount,
                awardsThisMonth,

                awardsTotal,
                round2(avgPerUser),
                round2(coverage),

                topPopularAllTime,
                topPopular30d,
                rarestActive,
                awardsByMonth
        );
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
