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
        int activeAchievements = achievementRepo.findAllActiveWithDeps().size();
        long blockedAchievements = totalAchievements - activeAchievements;
        long availableAchievements = activeAchievements;

        long sectionsCount = sectionRepo.findByActiveTrueOrderBySortOrderAsc().size();
        long activeUsers = Math.max(0, userRepo.countActive());

        Map<UUID, Long> awardedCounts = userAchRepo.countAwardedByAchievement()
                .stream()
                .collect(Collectors.toMap(
                        UserAchievementRepository.AwardStat::getAchievementId,
                        UserAchievementRepository.AwardStat::getAwardedCount
                ));
        long awardsTotal = awardedCounts.values().stream().mapToLong(Long::longValue).sum();

        double avgPerUser = activeUsers == 0 ? 0.0 : (double) awardsTotal / (double) activeUsers;

        long usersWithAnyAwards = userAchRepo.countUsersWithAnyAwards();
        double coverage = (activeUsers == 0) ? 0.0 : 100.0 * (double) usersWithAnyAwards / (double) activeUsers;

        Instant startOfMonth = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1)
                .atStartOfDay().toInstant(ZoneOffset.UTC);
        long awardsThisMonth = userAchRepo.countAwardedSince(startOfMonth);

        var topAllTime = userAchRepo.findTopAwardedAllTime(PageRequest.of(0, 5));
        Map<UUID, Achievement> byIdAll = achievementRepo.findAllById(
                topAllTime.stream().map(UserAchievementRepository.TopAward::getAchievementId).toList()
        ).stream().collect(Collectors.toMap(Achievement::getId, Function.identity()));
        List<TopAchievementDto> topPopularAllTime = topAllTime.stream()
                .map(t -> new TopAchievementDto(t.getAchievementId(),
                        Optional.ofNullable(byIdAll.get(t.getAchievementId())).map(Achievement::getTitle).orElse(""),
                        t.getCnt()))
                .toList();

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

        Instant from6m = YearMonth.now(ZoneOffset.UTC).minusMonths(5)
                .atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        var monthBuckets = userAchRepo.countAwardsByMonthSince(from6m);
        List<MonthlyCountDto> awardsByMonth = monthBuckets.stream()
                .map(mb -> new MonthlyCountDto(mb.getMonth(), mb.getCnt()))
                .toList();

        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        List<DailyCountDto> visitsByDay30d = syntheticVisitsForRange(todayUtc.minusDays(29), todayUtc);
        TrendDeltaDto visitsChange30d = computeTrendDelta(todayUtc);

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
                awardsByMonth,

                visitsByDay30d,
                visitsChange30d
        );
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }


    private TrendDeltaDto computeTrendDelta(LocalDate todayUtc) {
        List<DailyCountDto> curr = syntheticVisitsForRange(todayUtc.minusDays(29), todayUtc);
        List<DailyCountDto> prev = syntheticVisitsForRange(todayUtc.minusDays(59), todayUtc.minusDays(30));

        int currSum = curr.stream().mapToInt(DailyCountDto::count).sum();
        int prevSum = Math.max(0, prev.stream().mapToInt(DailyCountDto::count).sum());

        double pct = prevSum == 0 ? 0.0 : 100.0 * (currSum - prevSum) / (double) prevSum;
        pct = round1(pct);
        String signed = (pct >= 0 ? "+" : "") + pct + "%";
        return new TrendDeltaDto(currSum, prevSum, pct, signed);
    }


    private List<DailyCountDto> syntheticVisitsForRange(LocalDate fromInclusive, LocalDate toInclusive) {
        List<DailyCountDto> out = new ArrayList<>();
        for (LocalDate d = fromInclusive; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            out.add(new DailyCountDto(d, synthCountFor(d)));
        }
        return out;
    }

    private int synthCountFor(LocalDate d) {
        double base = 520.0;

        double w = (d.getDayOfWeek().getValue() % 7) / 7.0; // 0..~1
        double weekly = 80.0 * Math.sin(2 * Math.PI * w - 0.8); // сдвиг фазы

        double monthly = 40.0 * Math.sin(2 * Math.PI * ((d.getDayOfYear() % 30) / 30.0));

        long seed = d.toEpochDay() * 31 + 12345;
        SplittableRandom r = new SplittableRandom(seed);
        double noise = r.nextInt(-25, 26);

        int val = (int) Math.round(base + weekly + monthly + noise);
        return Math.max(120, Math.min(1500, val));
    }
}
