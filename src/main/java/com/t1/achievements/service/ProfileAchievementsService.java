package com.t1.achievements.service;

import com.t1.achievements.entity.*;
import com.t1.achievements.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileAchievementsService {

    private final UserRepository userRepo;
    private final SectionRepository sectionRepo;
    private final AchievementRepository achievementRepo;
    private final UserAchievementRepository userAchRepo;
    private final UserAchievementProgressRepository progressRepo;
    private final AchievementCriterionRepository criterionRepo;

    private String assetUrl(Asset a) {
        if (a == null) return null;
        return "/assets/" + a.getId();
    }

    public record UserDto(String fullName, String department, String position, String avatarUrl) {}

    /** ← вместо percentDone теперь ступени */
    public record AchievementCardDto(
            UUID id,
            String title,
            String iconUrl,
            int currentStep,
            int totalSteps,
            boolean awarded,
            double rarityPercent
    ) {}

    public record SectionDto(UUID id, String name, String description, List<AchievementCardDto> achievements) {}
    public record ProfileViewDto(UserDto user, int unlockedCount, List<SectionDto> sections) {}

    @Transactional(readOnly = true)
    public ProfileViewDto getProfileView(UUID userId) {
        User u = userRepo.findById(userId).orElseThrow();
        long totalUsers = Math.max(1, userRepo.countActive());

        Set<UUID> awarded = userAchRepo.findByUserId(userId).stream()
                .map(ua -> ua.getAchievement().getId())
                .collect(Collectors.toSet());

        // прогресс пользователя по ачивкам
        Map<UUID, UserAchievementProgress> progress = progressRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(p -> p.getAchievement().getId(), p -> p));

        int unlockedCount = awarded.size();

        Map<UUID, Long> awardedCounts = userAchRepo.countAwardedByAchievement().stream()
                .collect(Collectors.toMap(UserAchievementRepository.AwardStat::getAchievementId,
                        UserAchievementRepository.AwardStat::getAwardedCount));
        Function<UUID, Double> rarity = achId ->
                100.0 * (awardedCounts.getOrDefault(achId, 0L) / (double) totalUsers);

        List<Section> sections = sectionRepo.findByActiveTrueOrderBySortOrderAsc();
        List<Achievement> allAchievements = achievementRepo.findAllActiveWithDeps();

        // для тех ачивок, где у пользователя еще нет записи прогресса, надо знать totalSteps
        Set<UUID> achIds = allAchievements.stream().map(Achievement::getId).collect(Collectors.toSet());
        Map<UUID, Integer> totalStepsByAch = criterionRepo
                .sumRequiredByAchievementIds(achIds)
                .stream()
                .collect(Collectors.toMap(AchievementCriterionRepository.SumRequired::getAchievementId,
                        r -> Optional.ofNullable(r.getTotalRequired()).orElse(1)));

        Map<UUID, List<Achievement>> bySection = new HashMap<>();
        for (Achievement a : allAchievements) {
            for (Section s : a.getSections()) {
                bySection.computeIfAbsent(s.getId(), k -> new ArrayList<>()).add(a);
            }
        }

        Comparator<AchievementCardDto> cmp = Comparator
                .comparingInt((AchievementCardDto c) -> {
                    if (c.awarded) return 0;
                    return c.currentStep > 0 ? 1 : 2;
                })
                .thenComparingDouble(c -> c.rarityPercent)
                .thenComparing(c -> c.title, String.CASE_INSENSITIVE_ORDER);

        List<SectionDto> sectionDtos = new ArrayList<>();
        for (Section s : sections) {
            List<Achievement> list = bySection.getOrDefault(s.getId(), List.of());

            List<AchievementCardDto> cards = list.stream()
                    .map(a -> {
                        UUID aid = a.getId();
                        boolean has = awarded.contains(aid);
                        double r = rarity.apply(aid);

                        UserAchievementProgress up = progress.get(aid);
                        int total = (up != null) ? up.getTotalSteps()
                                : totalStepsByAch.getOrDefault(aid, 1);
                        int curr = (up != null) ? up.getCurrentStep() : 0;

                        // safety: total >= 1
                        total = Math.max(1, total);
                        curr = Math.min(curr, total);

                        return new AchievementCardDto(
                                aid,
                                a.getTitle(),
                                assetUrl(a.getIcon()),
                                curr,
                                total,
                                has,
                                r
                        );
                    })
                    .sorted(cmp)
                    .toList();

            sectionDtos.add(new SectionDto(
                    s.getId(), s.getName(), s.getDescription(), cards
            ));
        }

        UserDto userDto = new UserDto(
                u.getFullName(),
                u.getDepartment(),
                u.getPosition(),
                assetUrl(u.getAvatar())
        );

        return new ProfileViewDto(userDto, unlockedCount, sectionDtos);
    }
}
