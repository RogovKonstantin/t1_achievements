package com.t1.achievements.service;

import com.t1.achievements.dto.admin.*;
import com.t1.achievements.entity.*;
import com.t1.achievements.repository.AchievementCriterionRepository;
import com.t1.achievements.repository.AchievementRepository;
import com.t1.achievements.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAchievementQueryService {

    private final AchievementRepository achievementRepo;
    private final AchievementCriterionRepository criterionRepo;
    private final UserRepository userRepo;          // <-- добавили
    private final AssetStorageService assets;

    private String url(Asset a) { return a == null ? null : assets.publicUrl(a); }

    @Transactional(readOnly = true)
    public List<AchievementAdminFullDto> listAllAchievementsFull() {
        List<Achievement> achievements = achievementRepo.findAll();

        // totalSteps (агрегат)
        Set<UUID> achIds = achievements.stream().map(Achievement::getId).collect(Collectors.toSet());
        Map<UUID, Integer> totalStepsByAch = criterionRepo
                .sumRequiredByAchievementIds(achIds).stream()
                .collect(Collectors.toMap(
                        AchievementCriterionRepository.SumRequired::getAchievementId,
                        r -> Optional.ofNullable(r.getTotalRequired()).orElse(1)
                ));

        // подтянуть критерии (как у тебя)
        Map<UUID, List<AchievementCriterion>> critByAch = new HashMap<>();
        for (UUID aid : achIds) {
            List<AchievementCriterion> crits = criterionRepo.findByAchievementId(aid);
            crits.sort(Comparator.comparing(c -> Optional.ofNullable(c.getSortOrder()).orElse(100)));
            critByAch.put(aid, crits);
        }

        // --- НОВОЕ: держатели и проценты ---
        long activeUsers = Math.max(0, userRepo.countActive()); // защита от 0

        // achievementId -> holdersCount
        Map<UUID, Long> holdersByAch = new HashMap<>();
        for (Object[] row : userRepo.countHoldersGrouped()) {
            UUID aid = (UUID) row[0];
            Long cnt = (Long) row[1];
            holdersByAch.put(aid, cnt == null ? 0L : cnt);
        }

        List<AchievementAdminFullDto> result = new ArrayList<>(achievements.size());
        for (Achievement a : achievements) {
            List<SectionRefDto> sections = Optional.ofNullable(a.getSections())
                    .orElseGet(Set::of)
                    .stream()
                    .map(s -> new SectionRefDto(s.getId(), s.getCode(), s.getName()))
                    .toList();

            List<AchievementCriterionDto> criteria = critByAch.getOrDefault(a.getId(), List.of())
                    .stream()
                    .map(this::toCriterionDto)
                    .toList();

            int totalSteps = Math.max(1, totalStepsByAch.getOrDefault(a.getId(), 1));

            long holders = holdersByAch.getOrDefault(a.getId(), 0L);
            double holdersPercent = (activeUsers > 0)
                    ? round2(holders * 100.0 / activeUsers)
                    : 0.0;

            result.add(new AchievementAdminFullDto(
                    a.getId(),
                    a.getCode(),
                    a.getTitle(),
                    a.getShortDescription(),
                    a.getDescriptionMd(),
                    a.getPoints(),
                    a.getRepeatable(),
                    a.getVisibility() != null ? a.getVisibility().name() : null,
                    url(a.getIcon()),
                    url(a.getAnimation()),
                    holdersPercent    ,

                    a.getActive(),
                    a.getCreatedAt(),
                    a.getUpdatedAt(),
                    totalSteps,
                    criteria,
                    sections
            ));
        }
        return result;
    }

    private AchievementCriterionDto toCriterionDto(AchievementCriterion c) {
        ActivityType t = c.getActivityType();
        ActivityTypeRefDto typeDto = (t == null) ? null : new ActivityTypeRefDto(t.getId(), t.getCode(), t.getName());
        return new AchievementCriterionDto(
                c.getId(),
                typeDto,
                c.getRequiredCount(),
                c.getWithinDays(),
                c.getDescriptionOverride(),
                c.getSortOrder()
        );
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}

