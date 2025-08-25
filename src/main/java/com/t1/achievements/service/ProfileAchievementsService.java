package com.t1.achievements.service;

import com.t1.achievements.dto.view.*;
import com.t1.achievements.entity.*;
import com.t1.achievements.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProfileAchievementsService {

    private final UserRepository userRepo;
    private final SectionRepository sectionRepo;
    private final AchievementRepository achievementRepo;
    private final UserAchievementRepository userAchRepo;
    private final UserAchievementProgressRepository progressRepo;
    private final AchievementCriterionRepository criterionRepo;
    private final AssetStorageService assets;

    private String assetUrl(Asset a) {
        return assets.publicUrl(a);
    }

    @Transactional(readOnly = true)
    public ProfileViewDto getProfileView(UUID userId) {
        User u = userRepo.findById(userId).orElseThrow();

        long totalUsers = Math.max(1, userRepo.countActive());

        Set<UUID> awarded = userAchRepo.findByUserId(userId).stream()
                .map(ua -> ua.getAchievement().getId())
                .collect(Collectors.toSet());

        Map<UUID, UserAchievementProgress> progress = progressRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(p -> p.getAchievement().getId(), p -> p));

        Set<UUID> inProgress = progress.values().stream()
                .filter(p -> p.getCurrentStep() > 0)
                .map(p -> p.getAchievement().getId())
                .collect(Collectors.toSet());

        int unlockedCount = awarded.size();

        Map<UUID, Long> awardedCounts = userAchRepo.countAwardedByAchievement().stream()
                .collect(Collectors.toMap(
                        UserAchievementRepository.AwardStat::getAchievementId,
                        UserAchievementRepository.AwardStat::getAwardedCount));

        Function<UUID, Double> rarity = achId ->
                100.0 * (awardedCounts.getOrDefault(achId, 0L) / (double) totalUsers);

        List<Section> sections = sectionRepo.findByActiveTrueOrderBySortOrderAsc();

        List<Achievement> allRelevant = achievementRepo.findAllActiveWithDeps().stream()
                .filter(a -> {
                    UUID id = a.getId();
                    return awarded.contains(id) || inProgress.contains(id);
                })
                .toList();

        Set<UUID> achIds = allRelevant.stream().map(Achievement::getId).collect(Collectors.toSet());

        Map<UUID, Integer> totalStepsByAch = criterionRepo
                .sumRequiredByAchievementIds(achIds)
                .stream()
                .collect(Collectors.toMap(
                        AchievementCriterionRepository.SumRequired::getAchievementId,
                        r -> Optional.ofNullable(r.getTotalRequired()).orElse(1)
                ));

        Map<UUID, List<Achievement>> bySection = new HashMap<>();
        for (Achievement a : allRelevant) {
            for (Section s : a.getSections()) {
                bySection.computeIfAbsent(s.getId(), k -> new ArrayList<>()).add(a);
            }
        }

        Comparator<AchievementCardDto> cmp = Comparator
                .comparingInt((AchievementCardDto c) -> c.awarded() ? 0 : 1)
                .thenComparingDouble(AchievementCardDto::rarityPercent)
                .thenComparing(AchievementCardDto::title, String.CASE_INSENSITIVE_ORDER);

        List<SectionWithCardsDto> sectionDtos = new ArrayList<>();
        for (Section s : sections) {
            List<Achievement> list = bySection.getOrDefault(s.getId(), List.of());

            List<AchievementCardDto> cards = list.stream()
                    .map(a -> toCardDto(a, awarded, rarity, progress, totalStepsByAch))
                    .filter(c -> c.awarded() || c.currentStep() > 0)
                    .sorted(cmp)
                    .toList();

            if (cards.isEmpty()) continue;

            sectionDtos.add(new SectionWithCardsDto(
                    s.getId(), s.getName(), s.getDescription(), cards
            ));
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd--MM--yyyy");
        ApiUserDto userDto = new ApiUserDto(
                u.getFullName(),
                u.getDepartment(),
                u.getPosition(),
                assetUrl(u.getAvatar()),
                u.getPhone(),
                u.getHireDate() != null ? u.getHireDate().format(fmt) : null,
                u.getEmail()
        );

        return new ProfileViewDto(userDto, unlockedCount, sectionDtos);
    }

    @Transactional(readOnly = true)
    public ActivityFeedDto getActivity(UUID userId) {
        userRepo.findById(userId).orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND, "Пользователь не найден"));

        var list = userAchRepo.findByUserIdOrderByAwardedAtDesc(userId);

        var items = list.stream()
                .map(ua -> {
                    var a = ua.getAchievement();
                    return new ActivityEventDto(
                            a.getId(),
                            a.getTitle(),
                            assetUrl(a.getIcon()),
                            ua.getAwardedAt(),
                            "Получена ачивка «" + a.getTitle() + "»."
                    );
                })
                .toList();

        return new ActivityFeedDto(items);
    }

    @Transactional(readOnly = true)
    public SectionsViewDto getSectionsViewAll(UUID userId) {
        User u = userRepo.findById(userId).orElseThrow();

        long totalUsers = Math.max(1, userRepo.countActive());

        Set<UUID> awarded = userAchRepo.findByUserId(userId).stream()
                .map(ua -> ua.getAchievement().getId())
                .collect(Collectors.toSet());

        Map<UUID, UserAchievementProgress> progress = progressRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(p -> p.getAchievement().getId(), p -> p));

        Map<UUID, Long> awardedCounts = userAchRepo.countAwardedByAchievement().stream()
                .collect(Collectors.toMap(
                        UserAchievementRepository.AwardStat::getAchievementId,
                        UserAchievementRepository.AwardStat::getAwardedCount
                ));

        Function<UUID, Double> rarity = achId ->
                100.0 * (awardedCounts.getOrDefault(achId, 0L) / (double) totalUsers);

        List<Section> sections = sectionRepo.findByActiveTrueOrderBySortOrderAsc();
        List<Achievement> allAchievements = achievementRepo.findAllActiveWithDeps();

        Set<UUID> achIds = allAchievements.stream().map(Achievement::getId).collect(Collectors.toSet());

        Map<UUID, Integer> totalStepsByAch = criterionRepo
                .sumRequiredByAchievementIds(achIds)
                .stream()
                .collect(Collectors.toMap(
                        AchievementCriterionRepository.SumRequired::getAchievementId,
                        r -> Optional.ofNullable(r.getTotalRequired()).orElse(1)
                ));

        Map<UUID, List<Achievement>> bySection = new HashMap<>();
        for (Achievement a : allAchievements) {
            for (Section s : a.getSections()) {
                bySection.computeIfAbsent(s.getId(), k -> new ArrayList<>()).add(a);
            }
        }

        Comparator<AchievementCardDto> cmp = Comparator
                .comparingInt((AchievementCardDto c) -> {
                    if (c.awarded()) return 0;
                    return c.currentStep() > 0 ? 1 : 2;
                })
                .thenComparingDouble(AchievementCardDto::rarityPercent)
                .thenComparing(AchievementCardDto::title, String.CASE_INSENSITIVE_ORDER);

        List<SectionWithCardsDto> sectionDtos = new ArrayList<>();
        for (Section s : sections) {
            List<Achievement> list = bySection.getOrDefault(s.getId(), List.of());

            List<AchievementCardDto> cards = list.stream()
                    .map(a -> toCardDto(a, awarded, rarity, progress, totalStepsByAch))
                    .sorted(cmp)
                    .toList();

            sectionDtos.add(new SectionWithCardsDto(
                    s.getId(), s.getName(), s.getDescription(), cards
            ));
        }

        return new SectionsViewDto(sectionDtos);
    }

    private AchievementCardDto toCardDto(
            Achievement a,
            Set<UUID> awarded,
            Function<UUID, Double> rarity,
            Map<UUID, UserAchievementProgress> progress,
            Map<UUID, Integer> totalStepsByAch
    ) {
        UUID aid = a.getId();
        boolean has = awarded.contains(aid);
        double r = rarity.apply(aid);
        UserAchievementProgress up = progress.get(aid);
        int total = (up != null) ? up.getTotalSteps() : totalStepsByAch.getOrDefault(aid, 1);
        int curr = (up != null) ? up.getCurrentStep() : 0;
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
    }
}
